package enterprise;
public class Quantity {
    Product item;
    int required;
    
    public Quantity(Product p, int i){
        item=p;
        required=i;
    }
    
    public Product getItem(){
        return item;
    }
    public int getRequired(){
        return required;
    }
    
    public void setItem(Product p){
        item=p;
    }
    public void setRequired(int i){
        required=i;
    }
    
}
