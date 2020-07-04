package main.service;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Random;
import javax.imageio.ImageIO;
import lombok.SneakyThrows;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  private final String fileUploadFolder = "./upload/";
  private final String outputPathFolder = "/image/";

  private final int hashFolderNameLength = 2;
  private final int hashFolderNameAmount = 3;

  private final double imageMaxResolution = 800;
  private final int avatarResolution = 100;


  public String saveImage(MultipartFile image, boolean isAvatar) {
    if (!image.isEmpty()) {
      try {
        String imageName = image.getOriginalFilename();
        String hashPath = generatePath(hashFolderNameLength, hashFolderNameAmount);
        final String fileUploadPath = fileUploadFolder + hashPath + imageName;

        BufferedImage bufferedImage = ImageIO.read(image.getInputStream());

        ImageIO.write(
            isAvatar
                ? resizeAvatar(bufferedImage)
                : resizeImageIfNeeded(bufferedImage),
            "png", new File(fileUploadPath));

        return getOutputPath(hashPath, imageName);
      } catch (Exception e) {
        return "Вам не удалось загрузить  => " + e.getMessage();
      }
    } else {
      return null;
    }
  }

  private String generatePath(int folderNameLength, int foldersAmount) {
    String symbols = "abcdefghijklmnopqrstuv";
    StringBuilder path = new StringBuilder();
    Random random = new Random();
    for (int i = 0; i < foldersAmount; i++) {
      for (int j = 0; j < folderNameLength; j++) {
        int index = (int) (random.nextFloat() * symbols.length());
        path.append(symbols, index, index + 1);
      }
      path.append("/");
    }
    File file = new File(fileUploadFolder + path.toString());
    file.mkdirs();
    return path.toString();
  }

  private String getOutputPath(String path, String fileName) {
    return outputPathFolder + path
        .replace("/", "-")
        .substring(0, path.length() - 1) + "/" + fileName;
  }

  private BufferedImage resizeImageIfNeeded(BufferedImage bufferedImage) {
    int width = bufferedImage.getWidth();
    int height = bufferedImage.getHeight();
    if (width > imageMaxResolution || height > imageMaxResolution) {
      double coefficient = imageMaxResolution / Integer.max(width, height);
      return Scalr.resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT,
          (int) (width * coefficient), (int) (height * coefficient));
    }
    return bufferedImage;
  }

  private BufferedImage resizeAvatar(BufferedImage bufferedImage) {
    return Scalr
        .resize(bufferedImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT, avatarResolution,
            avatarResolution);
  }

  @SneakyThrows
  public byte[] getImageFromStorage(String path) {
    File file = new File(fileUploadFolder + path);
    return Files.readAllBytes(file.toPath());
  }
}
