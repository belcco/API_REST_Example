package com.example.dao;

import java.util.List;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.entities.Producto;

public interface ProductoDao extends JpaRepository<Producto, Long> {

    /**
     *      Vamos a necesitar tres metodos adicionales a los que genera el crud repository (interface),
     *      para:
     *      1) Recuperar la lista de los productos ordenados.
     *      2) Recuperar el listado de productos paginados, es decir, que no traiga todos los productos,
     *          sino de 10 en 10, de 20 en 20, etc.
     *      3) Recuperar las presentaciones con sus productos correspondientes sin tener que realizar una 
     *          subconsulta que seria menos eficiente que un join a las entidades utilizando hql (Hibernate
     *          Query Language).
     */
    

     @Query(value = "select p from Producto p left join fetch p.presentacion")
     public List<Producto> findAll(Sort sort);



    /**
     *      El siguiente metodo recupera una pagina de producto. 
     */

     @Query(value = "select p from Producto p left join fetch p.presentacion", 
            countQuery = "select count(p) from Producto p left join p.preentacion")

     public Page<Producto> findAll(Pageable pageable);


     /**
      *       El metodo siguiente recupera un producto por el id
      */
     @Query(value = "select p from Producto p left join fetch p.presentacion where p.id = :id")
      public Producto findById(long id);








}
