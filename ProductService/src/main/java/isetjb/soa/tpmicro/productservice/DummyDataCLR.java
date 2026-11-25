package isetjb.soa.tpmicro.productservice;

import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
class DummyDataCLR implements CommandLineRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(String... strings) throws Exception {
        // Jeu de données utilisé par les commandes du TP4 (prix et stock)
        Stream.of(
                new Product("Pencil", "Crayon HB", 1.50, 100),
                new Product("Book", "Cahier 200 pages", 12.00, 50),
                new Product("Eraser", "Gomme blanche", 0.75, 200))
                .forEach(productRepository::save);
        productRepository.findAll().forEach(s -> System.out.println(s.getName() + " - " + s.getPrice() + " DT"));
    }
}
