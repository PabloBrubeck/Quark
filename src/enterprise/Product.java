package enterprise;
public class Product {
    private int stock;
    private float cost;
    private float price;
    private String description;
    private String imageDir;
    
    public Product(int i, float c, float p, String d, String img){
        stock=i;
        cost=c;
        price=p;
        description=d;
        imageDir=img;
    }
    public Product(int i, float c, float p, String d){
        this(i, c, p, d, null);
    }
    
    public int getStock(){
        return stock;
    }
    public float getCost(){
        return cost;
    }
    public float getPrice(){
    return price;
    }
    public String getDescription(){
        return description;
    }
    public String getImageDir(){
        return imageDir;
    }
    
    public void setStock(int i){
        stock=i;
    }
    public void setCost(float c){
        cost=c;
    }
    public void setPrice(float p){
        price=p;
    }
    public void setDescription(String d){
        description=d;
    }
    public void setImageDir(String img){
        imageDir=img;
    }
}
