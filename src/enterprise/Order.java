package enterprise;
public class Order {
    private float cost;
    private long date;
    private Client client;
    private Quantity[] cart; 
    
    public Order(float f, long d, Client c, Quantity[] q){
        cost=f;
        date=d;
        client=c;
        cart=q;
    }
    public Order(float f, Client c, Quantity[] q) {
        this(f, System.currentTimeMillis(), c, q);
    }
    
    public float getCost(){
        return cost;
    }
    public long getDate(){
        return date;
    }
    public Client getClient(){
        return client;
    }   
    public Quantity[] getCart(){
        return cart;
    }
    
    public void setDate(long l){
        date=l;
    }
    public void setDate(){
        date=System.currentTimeMillis();
    }
    public void setCost(float f){
        cost=f;
    }
    public void setClient(Client c){
        client=c;
    }
    public void setCart(Quantity... q){
        cart=q;
    }
}