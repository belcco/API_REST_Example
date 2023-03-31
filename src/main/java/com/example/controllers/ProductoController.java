package com.example.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entities.Producto;
import com.example.servicies.ProductoService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

/**
 *  ANOTACIÓN DE LA API RES: hace que los métodos que ponemos a continuación devuelvan un json.
 *  La API RES gestiona un recurso. En dependencia del verbo HTTP que se utilice estás haciendo una petición
 *  completa.
 */
@RestController

/**
 *  Todas las peticiones son productos, en dependencia de como las haces (post - guarda un protucto, set - introduce datos, etc.)
 */
@RequestMapping("/productos")
public class ProductoController {

    /**
     *  Necesitamos un método que devuelva un listado de productos con paginación o no. 
     *  Una API RES tiene que responder la petición realizada al servidor web.
     *  Para ello se utiliza un enumerable. 
     *  Resumen: no basta con realizar una petición, sino que hay que dar respuesta a dicha petición.
     */

     @Autowired
     private ProductoService productoService;

     // El responseEntity es una entidad de respuesta, dentro del diamante lleva lo que quieres que responda.
     // En este caso la respuesta es una lista de productos y la confirmación.
    // Este método va a devolver los productos paginados o no.

    /**
     *  El método siguiente va a responder a una petición (request) del tipo: 
     *  http://localhost:8080/productos?page=1%size=4 (página 1 con 4 productos).
     *  Este método tiene que ser capaz de devolver un listado de productos paginados o no, pero 
     *  ordenado en cualquier caso ordenados por un criterio: nombre, descripción, ect. 
     *  La petición anteior implica @RequestParam
     * 
     *  /productos/id => @PathVariable
     *  
     */
    @GetMapping
     public ResponseEntity<List<Producto>> findAll(@RequestParam(name = "page", required = false) Integer page,
                                                    @RequestParam(name = "size", required = false) Integer size)    {

        // Para que devuelva un ResponseEntity hay que crear un objeto. 
        // Primero ponemos = null(después lo cambiamos) porque es necesario inicializarlo al estar dentro de la clase.
        ResponseEntity<List<Producto>> responseEntity = null;

        List<Producto> productos = new ArrayList<>();


        Sort sortByNombre = Sort.by("nombre");

        // No se puede poner != null porque habría que hacer un integer.
        if( page != null && size != null) {

            // Si la petición anterior es correcta y el num de paginas es distinto de cero es conveniente hacer 
            // un try catch.

            // CON PAGINACIÓN Y ORDENAMIENTO.
            try {
                // La tercera es la única opcion de PageRequest.of que tiene página, tamaño y criterio de ordenaxión (sort),
                Pageable pageable = PageRequest.of(page, size, sortByNombre);
                // Si el num de paginas es distinto de cero nos devuelve los productos como paginados.
                // que viene de la capa de servicios.
               
                Page<Producto> productosPaginados = productoService.findAll(pageable);
                productos = productosPaginados.getContent();
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);


            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } else {
            // SIN PAGINACIÓN Y CON ORDENAMIENTO
            try {
                productos = productoService.findAll(sortByNombre);
                responseEntity = new ResponseEntity<List<Producto>>(productos, HttpStatus.OK);

            } catch (Exception e) {
                responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return responseEntity;

     }

     /**
      *     Método que recupera un producto por el id, que responde a una petición del tipo:/
      *     http://localhost:8080/productos/id
      */

      @GetMapping("/{id}")
      public ResponseEntity<Map<String, Object>> findById(@PathVariable(name = "id") Integer id) {

        ResponseEntity<Map<String, Object>> responseEntity = null;
        Map<String, Object> responseAsMap = new HashMap<>();
        // HashMap no permite ordenamiento.

        try {

            Producto producto = productoService.findById(id);
            if (producto != null) {
                String successMessage = "Se ha encontrado el producto con id: " + id;
            responseAsMap.put("mensaje", successMessage);
            responseAsMap.put("producto", producto);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.OK);

            } else {

                String errorMessage = "No se ha encontrado el producto con id: " + id;
            responseAsMap.put("error", errorMessage);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap,HttpStatus.NOT_FOUND);

            }
            

        } catch (Exception e) {
            String errorGrave = "Error grave";
            responseAsMap.put("error", errorGrave);
            responseEntity = new ResponseEntity<Map<String, Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
            
        }

        return responseEntity;
        
      } 

      /**
       *    MÉTODO: persiste un producto en la base de datos.
       *    Este método en el cuerpo de la petición va a recibir un json. El producto viene dentro del protocolo de la petición.
       *    Cuando la anotación es Post va dentro de la petición y cuando es Get va fuera.
       *    El json se tiene que corresponder con el objeto de la petición, eso se consigue con RequestBody, y tenemos que
       *    crear la variable producto de tipo Producto. El producto se valida con la anotación @Valid
       *    (validar los errores que se corresponden con las condiciones de la clase producto, por ejemplo:
       *    el precio no puede ser negativo, el nombre tiene que tener entre 5 y 25 caracteres...) que sirve para
       *    validar que los produtos cumplen con las condiciones para devolver el producto. 
       *    Hay que añadir un método BindingResult y reflejarlo en una variable que la llamamos result (esto te permite
       *    ver los errores de la validación).
       */

       // Guardar (Persistir), un producto, con su presentacion en la base de datos
    // Para probarlo con POSTMAN: Body -> form-data -> producto -> CONTENT TYPE ->
    // application/json
    // no se puede dejar el content type en Auto, porque de lo contrario asume
    // application/octet-stream
    // y genera una exception MediaTypeNotSupported

       @PostMapping(consumes = "multipart/home-data")
       @Transactional
       public ResponseEntity<Map<String,Object>> insert(
        @Valid @RequestBody Producto producto, BindingResult result,
        @RequestParam(name = "file")MultipartFile file) {

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String,Object>> responseEntity = null;

        /**
         *  Primero: antes de guardar el producto en la base de datos hay que comprobar si hay errores en el producto recibido.
         */

         // Utilizamos un if para preguntar si hay errores. 
         if(result.hasErrors()) {

            List<String> errorMessages = new ArrayList<>();

            for( ObjectError error: result.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
            return responseEntity;
         }

         // Si no hay errores, guardamos el producto en lqa base de datos (persistimos el producto).
         
        
         Producto productoDB = productoService.save(producto);
         
         try {

            if(productoDB != null) {

                String mensaje = "El producto se ha creado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("producto", productoDB);
                responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.CREATED);
    
                } else {
    
                    // No se ha creado el producto.
                }

         } catch (DataAccessException e) {
            String errorGrave = "Se ha producido un error grave y la causa puede ser " + e.getMostSpecificCause();
            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
         }
      
         
        
         return responseEntity;

        

       }


       /**
        *   METODO: Actualiza un producto en la base de datos.
        */



       @PutMapping("/{id}")
       @Transactional
       public ResponseEntity<Map<String,Object>> update(@Valid @RequestBody Producto producto,
       BindingResult result, @PathVariable(name = "id") Integer id) {

        Map<String, Object> responseAsMap = new HashMap<>();
        ResponseEntity<Map<String,Object>> responseEntity = null;

        /**
         *  Primero: antes de guardar el producto en la base de datos hay que comprobar si hay errores en el producto recibido.
         */

         // Utilizamos un if para preguntar si hay errores. 
         if(result.hasErrors()) {

            List<String> errorMessages = new ArrayList<>();

            for( ObjectError error: result.getAllErrors()) {
                errorMessages.add(error.getDefaultMessage());
            }

            responseAsMap.put("errores", errorMessages);

            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.BAD_REQUEST);
            return responseEntity;
         }

         // Si no hay errores, actualizamos el producto. 
         // Vinculando, previamente, el id que se recibe con el producto.

         producto.setId(id);
         Producto productoDB = productoService.save(producto);
         
         try {

            if(productoDB != null) {

                String mensaje = "El producto se ha creado correctamente";
                responseAsMap.put("mensaje", mensaje);
                responseAsMap.put("producto", productoDB);
                responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.CREATED);
    
                } else {
    
                    // No se ha actualizado el producto.
                }

         } catch (DataAccessException e) {
            String errorGrave = "Se ha producido un error grave y la causa puede ser " + e.getMostSpecificCause();
            responseAsMap.put("errorGrave", errorGrave);
            responseEntity = new ResponseEntity<Map<String,Object>>(responseAsMap, HttpStatus.INTERNAL_SERVER_ERROR);
         }
      
         
        
         return responseEntity;

       }

         /**
        *   METODO: Borra un producto de la base de datos.
        */

        @DeleteMapping("/{id}")
        @Transactional
        public ResponseEntity<String> delete(@PathVariable(name = "id") Integer id) {

            // Para borrar el producto primero hay que recuperarlo. 
        ResponseEntity<String> responseEntity = null;

        try {
            // Recuperamos el producto
            Producto producto = productoService.findById(id);
            // Si lo ha encontrado (el producto es distinto de null):
            if ( producto != null) {
                // entonces lo borra
                productoService.delete(producto);
                responseEntity = new ResponseEntity<String>("Borrado exitosamente", HttpStatus.OK);
            } else {
            // Si no ha encontrado el producto:
                responseEntity = new ResponseEntity<String>("Producto no encontrado", HttpStatus.NOT_FOUND);
            }


        } catch (DataAccessException e) {
            e.getMostSpecificCause();
            responseEntity = new ResponseEntity<String>("Error fatal", HttpStatus.INTERNAL_SERVER_ERROR);
        }

         return responseEntity;

    }




    /**
     *  El método siguiente es de ejemplo para entender el formato JSON, no tiene que ver en sí con el proyecto.
     *  La anotación es para que al lanzar el servidor y hacer la petición se muestre un json. 
     */
    //  @GetMapping
    //  public List<String> nombres() {
    //     List<String> nombres = Arrays.asList("Salma", "Judith", "Elisabeth");
    //     return nombres;
    //  }
    
}
