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
    private static final String path = "/media/lokitha/Data Drive/Projects/SE10 Social Network/SE10-NETWORK-BACKEND/src/main/resources/static/";
    private static final String[] formats = {".jpg", ".png", ".jpeg"};

    /**
     * Saves the uploaded image file in the user's directory. If an image with the same name already exists,
     * it will be replaced. The file name will follow the pattern `userid_type_photo.extension`.
     *
     * @param userId        The ID of the user.
     * @param type          The type of image (e.g., "profile" or "cover").
     * @param multipartFile The image file to be saved.
     * @throws IOException If an error occurs during file saving.
     */
    public static void saveFile(Long userId, String type, MultipartFile multipartFile) throws IOException {
        File staticDir = new File(path);
        if (!staticDir.exists()) {
            staticDir.mkdirs();
            logger.info("Created static directory: {}", path);
        }
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
            logger.error("Failed to delete existing image file: {}", fileNameWithoutExtension, ioException);
            throw new IOException("Failed to delete existing image file: " + fileNameWithoutExtension, ioException);
        }

        try (InputStream inputStream = multipartFile.getInputStream()) {
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved image file: {}", fileName);
        } catch (IOException ioException) {
            logger.error("Failed to save image file: {}", fileName, ioException);
            throw new IOException("Failed to save image file: " + fileName, ioException);
        }
    }

    /**
     * Retrieves and encodes the user's profile and cover images as Base64 strings.
     * The encoded images are then set in the provided UserDTO.
     *
     * @param userDTO The UserDTO to populate with image data.
     * @return The updated UserDTO containing Base64-encoded image data.
     */
    public static UserDTO getUserImages(UserDTO userDTO) {
        String profilePic = getProfileImage(userDTO.getUserId());
        String coverPic = getCoverImage(userDTO.getUserId());

        if (profilePic != null) {
            userDTO.setProfileImg(profilePic);
        }
        if (coverPic != null) {
            userDTO.setCoverImg(coverPic);
        }
        return userDTO;
    }

    /**
     * Searches for and retrieves the user's profile image based on their userId.
     * Checks multiple formats (e.g., .jpg, .png, .jpeg) in the user's directory.
     *
     * @param userId The ID of the user.
     * @return The Base64-encoded profile image if found; otherwise, null.
     */
    public static String getProfileImage(Long userId) {
        logger.debug("Searching for profile photo for user {}", userId);

        try {
            for (String format : formats) {
                File profilePic = new File(path + userId + "/" + userId + "_profile_photo" + format);

                if (profilePic.exists()) {
                    logger.info("Profile photo found for user {}", userId);
                    return encodeFileToBase64(profilePic);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to retrieve profile photo for user {}", userId, e);
        }

        logger.warn("No profile photo found for user {}", userId);
        return null;
    }

    /**
     * Searches for and retrieves the user's cover image based on their userId.
     * Checks multiple formats (e.g., .jpg, .png, .jpeg) in the user's directory.
     *
     * @param userId The ID of the user.
     * @return The Base64-encoded cover image if found; otherwise, null.
     */
    public static String getCoverImage(Long userId) {
        logger.debug("Searching for cover photo for user {}", userId);

        try {
            for (String format : formats) {
                File coverPic = new File(path + userId + "/" + userId + "_cover_photo" + format);

                if (coverPic.exists()) {
                    logger.info("Cover photo found for user {}", userId);
                    return encodeFileToBase64(coverPic);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to retrieve cover photo for user {}", userId, e);
        }

        logger.warn("No cover photo found for user {}", userId);
        return null;
    }

    /**
     * Deletes a user's image file based on the specified type (e.g., "profile" or "cover").
     *
     * @param userId The ID of the user.
     * @param type   The type of image to delete (e.g., "profile" or "cover").
     * @throws IOException If an error occurs during file deletion.
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
            logger.error("Failed to delete image file: {}", fileNameWithoutExtension, ioException);
            throw new IOException("Failed to delete image file: " + fileNameWithoutExtension, ioException);
        }
    }

    /**
     * Encodes a file to a Base64-encoded string.
     *
     * @param file The file to encode.
     * @return The Base64-encoded string.
     * @throws IOException If an error occurs during encoding.
     */
    private static String encodeFileToBase64(File file) throws IOException {
        byte[] fileContent = FileUtils.readFileToByteArray(file);
        logger.debug("File encoded to Base64: {}", file.getName());
        return Base64.getEncoder().encodeToString(fileContent);
    }

    /**
     * Extracts the file extension from a given file name.
     *
     * @param fileName The name of the file.
     * @return The file extension as a string.
     */
    private static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
}
