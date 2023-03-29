package com.example.servicies;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.dao.ProductoDao;
import com.example.entities.Producto;

// Un bean es un objeto manejado por Spring. Spring coge todas las clases y las mete en el controlador de inmersion de control
// cuando la aplicacion arranca el programa va buscando las diferentes anotaciones. 
// Si no ponemos la anotacion service no se puede inyectar pq. Spring va de inyeccion de dependencias e inyeccion de control.

/**
 *  Inyectar una dependencia es meter un objeto sin tener que crearlo.  
 */

@Service
public class ProductoServiceImpl implements ProductoService {

    // Creamos una variable para implementar el dao y anotamos con autowired que es para cuando haga falta
    // la anotacion permite que spring lo inyecte. Hay resolucion de dependencias por autowired y por constructor. 
    @Autowired
    private ProductoDao productoDao;

    @Override
    public List<Producto> findAll(Sort sort) {
        return productoDao.findAll(sort);
    }

    @Override
    public Page<Producto> findAll(Pageable pageable) {
        return productoDao.findAll(pageable);
    }

    @Override
    public Producto findById(long id) {
        return productoDao.findById(id);
    }

    @Override
    public Producto save(Producto producto) {
       return productoDao.save(producto);
    }

    @Override
    public void delete(Producto producto) {
        
    }
    
    
    
}
