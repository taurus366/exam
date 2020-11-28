package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softuni.exam.models.dto.xmls.SellerImportDto;
import softuni.exam.models.dto.xmls.SellerRootImportDto;
import softuni.exam.models.entity.Rating;
import softuni.exam.models.entity.Seller;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.SellerService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Transactional
public class SellerServiceImpl implements SellerService {

    private final static String SELLER_PATH="src/main/resources/files/xml/sellers.xml";

    @Autowired
    private final SellerRepository sellerRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final XmlParser xmlParser;
    @Autowired
    private final ValidationUtil validationUtil;

    public SellerServiceImpl(SellerRepository sellerRepository, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil) {
        this.sellerRepository = sellerRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
    }


    @Override
    public boolean areImported() {
        return this.sellerRepository.count()>0;
    }

    @Override
    public String readSellersFromFile() throws IOException {
        return String.join("", Files.readAllLines(Path.of(SELLER_PATH)));
    }

    @Override
    public String importSellers() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        SellerRootImportDto sellerRootImportDto = this.xmlParser.parseXml(SellerRootImportDto.class, SELLER_PATH);

        for (SellerImportDto sellerImportDto : sellerRootImportDto.getSellerImportDtos()) {
                Rating rating;
            try {
                rating = Rating.valueOf(sellerImportDto.getRating());

              //  seller.setRating(Rating.valueOf(sellerImportDto.getRating()));
            }catch (Exception e){
                sb.append("Invalid seller").append(System.lineSeparator());
                continue;
            }

            Optional<Seller> byEmail = this.sellerRepository.findByEmail(sellerImportDto.getEmail());

            //Rating.valueOf(sellerImportDto.getRating()); // -> VERY_GOOD -> EXCEPTION

            if (this.validationUtil.isValid(sellerImportDto) && byEmail.isEmpty()){

            Seller seller = this.modelMapper.map(sellerImportDto,Seller.class);
            seller.setRating(rating);

            this.sellerRepository.saveAndFlush(seller);
            sb.append(String
                    .format("Successfully import seller %s - %s", seller.getLastName(),seller.getEmail())).append(System.lineSeparator());

            }else {
                    sb.append("Invalid seller").append(System.lineSeparator());
            }
        }


        return sb.toString();
    }
}
