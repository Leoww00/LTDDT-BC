package service;

import java.util.*;
import model.Food;
public class FoodService {
    private final List<Food> foods=new ArrayList<>();
    public FoodService(){
        foods.add(new Food("M01","Com ga",35000,"Com",true));
        foods.add(new Food("M02","Bun bo",40000,"Bun",true));
        foods.add(new Food("M03","Banh mi",20000,"Banh",true));
        foods.add(new Food("M04","Tra sua",30000,"Nuoc",false));
        foods.add(new Food("M05","Pho bo",45000,"Pho",true));
    }
    public List<Food> getAll(){return Collections.unmodifiableList(foods);}
    public Food findById(String id){for(Food f:foods)if(f.getId().equalsIgnoreCase(id))return f;throw new IllegalArgumentException("Khong tim thay ma mon: "+id);}
    public List<Food> search(String keyword){List<Food> out=new ArrayList<>();for(Food f:foods)if(f.getName().toLowerCase().contains(keyword.toLowerCase().trim()))out.add(f);return out;}
}
