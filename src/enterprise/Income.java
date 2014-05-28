package enterprise;
public class Income {
    private float amount;
    private String description;
    private long date;
    
    public Income(float f, String d, long l) {
       amount=f;
       description=d;
       date=l;
    }
    
    public Income(float f, String d) {
        this(f, d, System.currentTimeMillis());
    }
    
    public float getAmount(){
        return amount;
    }
    public String getDescription(){
        return description;
    }
    public long getDate(){
        return date;
    }
    
    public void setAmount(float f){
        amount=f;
    }
    public void setDescription(String d){
        description=d;
    }
    public void setDate(long l){
        date=l;
    }
    public void setDate(){
        date=System.currentTimeMillis();
    }
}
