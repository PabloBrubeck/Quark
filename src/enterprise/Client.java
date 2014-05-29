package enterprise;

public class Client {
    
    
    private String name, organization, phone, address;
    private Order[] history;
    
    public Client(String n, String org, String p, String a, Order... h){
        name=n;
        organization=org;
        phone=p;
        address=a;
        history=h;       
    }
    
    
    public String getName(){
        return name;
    }
    public String getOrganization(){
        return organization;
    }
    public String getPhone(){
        return phone;
    }
    public String getAddress(){
        return address;
    }
    public Order[] getHistory(){
        return history;
    }
            
    public void setName(String n){
        name=n;
    }
    public void setOrganization(String org){
        organization=org;
    }
    public void setPhone(String p){
        phone=p;
    }
    public void setAddress(String a){
        address=a;
    }
    public void setHistory(Order... h){
        history=h;
    }

}