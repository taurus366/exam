package softuni.exam.service.impl;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softuni.exam.models.dto.xmls.OfferImportDto;
import softuni.exam.models.dto.xmls.OfferImportRootDto;
import softuni.exam.models.entity.Car;
import softuni.exam.models.entity.Offer;
import softuni.exam.models.entity.Seller;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.OfferRepository;
import softuni.exam.repository.SellerRepository;
import softuni.exam.service.OfferService;
import softuni.exam.util.ValidationUtil;
import softuni.exam.util.XmlParser;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;

@Service
@Transactional
public class OfferServiceImpl implements OfferService {
    private final static String OFFER_PATH = "src/main/resources/files/xml/offers.xml";

    @Autowired
    private final OfferRepository offerRepository;
    @Autowired
    private final ModelMapper modelMapper;
    @Autowired
    private final XmlParser xmlParser;
    @Autowired
    private final ValidationUtil validationUtil;
    @Autowired
    private final CarRepository carRepository;
    @Autowired
    private final SellerRepository sellerRepository;

    public OfferServiceImpl(OfferRepository offerRepository, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil, CarRepository carRepository, SellerRepository sellerRepository) {
        this.offerRepository = offerRepository;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.carRepository = carRepository;
        this.sellerRepository = sellerRepository;
    }


    @Override
    public boolean areImported() {
        return this.offerRepository.count()>0;
    }

    @Override
    public String readOffersFileContent() throws IOException {
        return String.join("", Files.readAllLines(Path.of(OFFER_PATH)));
    }

    @Override
    public String importOffers() throws IOException, JAXBException {
        StringBuilder sb = new StringBuilder();

        OfferImportRootDto offerImportRootDto = this.xmlParser.parseXml(OfferImportRootDto.class, OFFER_PATH);
        for (OfferImportDto offerImportDto : offerImportRootDto.getOfferImportDtos()) {

          if (validationUtil.isValid(offerImportDto)){
              Offer offer = this.modelMapper.map(offerImportDto, Offer.class);

              Car car = this.carRepository.findById(offerImportDto.getCar().getId()).get();
              Seller seller = this.sellerRepository.findById(offerImportDto.getSeller().getId()).get();



              offer.setPictures(new HashSet<>(car.getPictures()));
              offer.setCar(car);
              offer.setSeller(seller);

              this.offerRepository.saveAndFlush(offer);
              sb.append(String.format("Successfull Import offer %s - %s",offer.getAddedOn(),offer.isHasGoldStatus()))
                      .append(System.lineSeparator());

          }else {
              sb.append("Invalid offer").append(System.lineSeparator());
          }

        }

        return sb.toString();
    }
}
