package doit.file.controller;

import doit.file.repository.Image;
import doit.file.repository.ImageRepository;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ImageController {

    private final ImageRepository imageRepository;
    private final static Path uploadPath = Path.of("/Users/sangjun/study/SpringReact/file/storage/");

    @PostMapping("/upload")
    public void upload(@RequestParam MultipartFile file) throws IOException {
        File destinationDirectory = uploadPath.toFile();
        if (!destinationDirectory.exists()) {
            destinationDirectory.mkdirs();
        }

        File destinationFile = new File(destinationDirectory, file.getOriginalFilename());
        file.transferTo(destinationFile);

        Image image = createImage(file.getOriginalFilename());
        imageRepository.save(image);
    }

    @GetMapping("/image")
    public ResponseEntity<byte[]> getImage(@RequestParam String imageName) throws IOException {
        File imageFile = uploadPath.resolve(imageName).toFile();

        if (!imageFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileInputStream fis = new FileInputStream(imageFile);
        byte[] imageData = fis.readAllBytes();
        fis.close();

        MediaType mediaType = getMediaTypeForFileName(imageName);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(imageData);
    }

    private static Image createImage(String imageName) {
        return Image.builder()
                .name(imageName)
                .path(uploadPath.resolve(imageName).toString()) // resolve 사용
                .build();
    }

    private MediaType getMediaTypeForFileName(String fileName) {
        String fileExtension = StringUtils.getFilenameExtension(fileName);

        if (fileExtension != null) {
            return switch (fileExtension.toLowerCase()) {
                case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
                case "png" -> MediaType.IMAGE_PNG;
                case "gif" -> MediaType.IMAGE_GIF;
                default -> MediaType.APPLICATION_OCTET_STREAM;
            };
        }

        return MediaType.APPLICATION_OCTET_STREAM; // 기본값
    }
}
