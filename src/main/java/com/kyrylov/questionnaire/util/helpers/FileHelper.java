package com.kyrylov.questionnaire.util.helpers;

import com.kyrylov.questionnaire.persistence.domain.entities.Document;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Classs to works with files
 *
 * @author Dmitrii
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class FileHelper {

    /**
     * Create folder according passed folders sequence, save new file to server and return its path
     *
     * @param fileName name of new file
     * @param content  content of file
     * @param folders  folders to save sequence
     * @return path of the new file
     * @throws IOException if something go wrong
     */
    public static String saveFileOnServerAndGetPath(final String fileName, final byte[] content, final String... folders)
            throws IOException {
        if (fileName != null && !fileName.isEmpty() && content != null) {
            StringBuilder sb = new StringBuilder();

            sb.append(FileHelper.getQuestionnaireFileSavePath());
            if (folders != null) {
                for (String appendFolder : folders) {
                    sb.append("\\").append(appendFolder);
                }
            }

            File folder = new File(sb.toString());

            return writeFileToFolder(fileName, folder, content);
        } else {
            return null;
        }
    }

    /**
     * Save file to server`s folder and return its path
     *
     * @param fileName name of file
     * @param folder   folder to save
     * @param content  file content
     * @return file`s save path
     * @throws IOException if something go wrong
     */
    public static String writeFileToFolder(final String fileName, final File folder, final byte[] content) throws IOException {
        File f = new File(folder, fileName);
        if (!f.exists()) {
            boolean isCreatedDirs = new File(f.getParent()).mkdirs();
            boolean isCreatedFile = f.createNewFile();

            log.debug("Directories were created {}", isCreatedDirs);
            log.debug("File was created {}", isCreatedFile);
        }
        log.debug("File exists {}", f.exists());

        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(content);
        }

        return f.getAbsolutePath();
    }

    public static Document loadDocumentContent(Document document) throws IOException {
        if (document != null && document.getFilePath() != null && !document.getFilePath().isEmpty()) {
            byte[] content = loadContentByPath(document.getFilePath());
            document.setContent(content);
        }
        return document;
    }

    public static byte[] loadContentByPath(final String path) throws IOException {
        if (path != null && !path.isEmpty()) {
            FileInputStream fileInputStream;
            File file = new File(path);
            if (file.exists()) {
                byte[] data = new byte[(int) file.length()];

                fileInputStream = new FileInputStream(file);
                fileInputStream.read(data);
                fileInputStream.close();

                return data;
            }
        }
        return null;
    }

    public static String getFileExtension(final String path) {
        return path != null ? path.substring(path.lastIndexOf('.')) : null;
    }

    private static String getQuestionnaireFileSavePath() throws IOException {
        return ResourceHelper.getProperties(ResourceHelper.ResourceProperties.PROJECT_PROPERTIES)
                .getProperty(ResourceHelper.ResourceProperties.ProjectProperties.QUESTIONNAIRE_FILE_SAVE_PATH);
    }

}
