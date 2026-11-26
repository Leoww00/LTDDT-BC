package repository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import model.*;
public class FileRepository {
    private final Path file;
    public FileRepository(String filename){file=Paths.get(filename);}
    public void save(Order order) throws IOException {
        // TSV: mot dong thong tin don + cac dong chi tiet; ghi UTF-8.
        if(file.getParent()!=null)Files.createDirectories(file.getParent());
        try(BufferedWriter out=Files.newBufferedWriter(file,StandardCharsets.UTF_8,StandardOpenOption.CREATE,StandardOpenOption.APPEND)){
            out.write("ORDER\t"+clean(order.getId())+"\t"+clean(order.getStudentId())+"\t"+order.getCreatedAt()+"\t"+order.getTotal()+"\t"+clean(order.getPaymentMethod())+"\t"+clean(order.getStatus()));out.newLine();
            for(OrderDetail d:order.getDetails()){out.write("ITEM\t"+clean(order.getId())+"\t"+clean(d.getFoodId())+"\t"+clean(d.getFoodName())+"\t"+d.getQuantity()+"\t"+d.getUnitPrice());out.newLine();}
        }
    }
    private String clean(String s){return s.replace('\t',' ').replace('\n',' ').replace('\r',' ');}
    public java.util.List<String> readAll() throws IOException {
        if(!Files.exists(file))return java.util.Collections.emptyList();
        return Files.readAllLines(file,StandardCharsets.UTF_8);
    }
}
