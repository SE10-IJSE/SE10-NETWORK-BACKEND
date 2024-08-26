package lk.ijse.SE10_NETWORK_BACKEND.util;

import lk.ijse.SE10_NETWORK_BACKEND.dto.UserDTO;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Base64;
import java.util.Objects;

public class ImageUploadUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageUploadUtil.class);
    private static final String path = "C:\\Users\\User\\Documents\\Code\\SE10 Social Network\\SE10-NETWORK-BACKEND\\src\\main\\resources\\static\\";

    /**
     * Save the uploaded file to the user's directory. If a file with the same name already exists,
     * it will be replaced. The file name will be standardized as `userid_type_photo.extension`.
     *
     * @param userId       The ID of the user.
     * @param type         The type of image (e.g., profile or cover).
     * @param multipartFile The file to be saved.
     * @throws IOException If an error occurs while saving the file.
     */
    public static void saveFile(Long userId, String type, MultipartFile multipartFile) throws IOException {
        String uploadDir = path + userId;
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Created directory for user {}: {}", userId, uploadDir);
        }

        String extension = getFileExtension(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        String fileNameWithoutExtension = userId + "_" + type + "_photo";
        String fileName = fileNameWithoutExtension + "." + extension;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(uploadPath, fileNameWithoutExtension + ".*")) {
            for (Path existingFile : stream) {
                Files.delete(existingFile);
                logger.info("Deleted existing file: {}", existingFile.getFileName());
            }
        } catch (IOException ioException) {
            logger.error("Could not delete existing image file: {}", fileNameWithoutExtension, ioException);
            throw new IOException("Could not delete existing image file: " + fileNameWithoutExtension, ioException);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved image file: {}", fileName);
        } catch (IOException ioException) {
            logger.error("Could not save image file: {}", fileName, ioException);
            throw new IOException("Could not save image file: " + fileName, ioException);
        }
    }

    /**
     * Retrieve and encode the user's profile and cover images as Base64 strings.
     * If an image file exists, it is added to the UserDTO.
     *
     * @param userDTO The user data transfer object to populate with image data.
     * @return The updated UserDTO with Base64-encoded image data.
     */
    public static UserDTO getUserImages(UserDTO userDTO) {
        try {
            String[] formats = {".jpg", ".png", ".jpeg"};

            for (String format : formats) {
                File profilePic = new File(path + userDTO.getUserId() + "\\" + userDTO.getUserId() + "_profile_photo" + format);
                File coverPic = new File(path + userDTO.getUserId() + "\\" + userDTO.getUserId() + "_cover_photo" + format);

                if (profilePic.exists()) {
                    logger.info("Profile photo found for user {}", userDTO.getUserId());
                    userDTO.setProfileImg(encodeFileToBase64(profilePic));
                }

                if (coverPic.exists()) {
                    logger.info("Cover photo found for user {}", userDTO.getUserId());
                    userDTO.setCoverImg(encodeFileToBase64(coverPic));
                }
            }
        } catch (IOException e) {
            logger.error("Error retrieving images for user {}", userDTO.getUserId(), e);
        }
        return userDTO;
    }

    /**
     * Delete a user's image file based on the image type (e.g., profile or cover).
     *
     * @param userId The ID of the user.
     * @param type   The type of image to delete (e.g., profile or cover).
     * @throws IOException If an error occurs while deleting the file.
     */
    public static void deleteFile(Long userId, String type) throws IOException {
        String directory = path + userId;
        String fileNameWithoutExtension = userId + "_" + type + "_photo";
        Path dirPath = Paths.get(directory);

        if (!Files.exists(dirPath)) {
            logger.warn("Directory does not exist: {}", directory);
            throw new IOException("Directory does not exist: " + directory);
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath, fileNameWithoutExtension + ".*")) {
            boolean fileDeleted = false;
            for (Path file : stream) {
                Files.delete(file);
                fileDeleted = true;
                logger.info("Deleted file: {}", file.getFileName());
            }

            if (!fileDeleted) {
                logger.warn("File not found: {}", fileNameWithoutExtension);
                throw new IOException("File not found: " + fileNameWithoutExtension);
            }
        } catch (IOException ioException) {
            logger.error("Could not delete image file: {}", fileNameWithoutExtension, ioException);
            throw new IOException("Could not delete image file: " + fileNameWithoutExtension, ioException);
        }
    }

    /**
     * Encode a file to a Base64 string.
     *
     * @param file The file to encode.
     * @return The Base64-encoded string.
     * @throws IOException If an error occurs while encoding the file.
     */
    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        logger.debug("File encoded to Base64: {}", file.getName());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Extract the file extension from a file name.
     *
     * @param fileName The name of the file.
     * @return The file extension.
     */
    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
