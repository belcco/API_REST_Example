package com.example.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUploadUtil {


    public  String saveFile(String fileName, MultipartFile multipartFile)
    throws IOException {
Path uploadPath = Paths.get("Files-Upload");

if (!Files.exists(uploadPath)) {
    Files.createDirectories(uploadPath);
}

// Conserva el nombre del archivo y lo prefija con un codigo aleatorio de 8 caracteres para que puedan haber
// archivos repetidos con el mismo nombre, ya que al añadirle el código los cifra de forma diferente a cada uno. 
String fileCode = RandomStringUtils.randomAlphanumeric(8);

// Try-with-resources. Se ponen recursos que se puedan abrir y cuando se sale del try automáticamente lo cierra, 
// sin tener que meter un finally. Los recursos que se pueden manejar son los que implementan la interfaz closeable
try (InputStream inputStream = multipartFile.getInputStream()) {
    Path filePath = uploadPath.resolve(fileCode + "-" + fileName);
    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
} catch (IOException ioe) {
    throw new IOException("Could not save file: " + fileName, ioe);
}

return fileCode;
}
    
}
