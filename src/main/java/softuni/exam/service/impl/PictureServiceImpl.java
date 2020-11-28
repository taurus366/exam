package softuni.exam.service.impl;

import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import softuni.exam.models.dto.jsons.PictureImportDto;
import softuni.exam.models.entity.Picture;
import softuni.exam.repository.CarRepository;
import softuni.exam.repository.PictureRepository;
import softuni.exam.service.PictureService;
import softuni.exam.util.ValidationUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
@Transactional
public class PictureServiceImpl implements PictureService {

   private final static String PICTURES_PATH="src/main/resources/files/json/pictures.json";

   @Autowired
    private final PictureRepository pictureRepository;
   @Autowired
   private final ModelMapper modelMapper;
   @Autowired
   private final Gson gson;
   @Autowired
   private final ValidationUtil validationUtil;
   @Autowired
   private final CarRepository carRepository;

    public PictureServiceImpl(PictureRepository pictureRepository, ModelMapper modelMapper, Gson gson, ValidationUtil validationUtil, CarRepository carRepository) {
        this.pictureRepository = pictureRepository;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.carRepository = carRepository;
    }

    @Override
    public boolean areImported() {
        return this.pictureRepository.count()>0;
    }

    @Override
    public String readPicturesFromFile() throws IOException {
        return String.join("", Files.readAllLines(Path.of(PICTURES_PATH)));
    }

    @Override
    public String importPictures() throws IOException {
        StringBuilder sb = new StringBuilder();
        PictureImportDto[] pictureImportDtos = this.gson.fromJson(this.readPicturesFromFile(), PictureImportDto[].class);
        for (PictureImportDto pictureImportDto : pictureImportDtos) {
            Optional<Picture> byName = this.pictureRepository.findByName(pictureImportDto.getName());

            if (this.validationUtil.isValid(pictureImportDto) && byName.isEmpty()){
                    Picture picture = this.modelMapper.map(pictureImportDto, Picture.class);
                    picture.setCar(this.carRepository.getOne(pictureImportDto.getCar()));

                this.pictureRepository.saveAndFlush(picture);
                sb.append(String.format("Succesfully import picture - %s", pictureImportDto.getName()))
                .append(System.lineSeparator());

            }else {
                sb.append("Invalid picture")
                        .append(System.lineSeparator());
            }

        }
        return sb.toString();
    }
}
