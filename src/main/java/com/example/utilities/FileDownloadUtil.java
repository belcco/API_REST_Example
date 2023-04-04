package com.example.utilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

// Para que pueda buscar el bean e inyecte el objeto donde se necesite. 
@Component

public class FileDownloadUtil {

    // Método para que devuelva la imagen cuando se le pida. 
    private Path foundFile;

    public Resource getFileAsResource(String fileCode) throws IOException {

        Path dirPath = Paths.get("Files-Upload");

        Files.list(dirPath).forEach(file -> {
            if(file.getFileName().toString().startsWith(fileCode)) {
                foundFile = file;

                return ;
            }
        });

        if(foundFile != null) {
            return new UrlResource(foundFile.toUri());
        }

        return null;
    }
}
