import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class User {

    private final String username;
    private JsonArray shoppingCart = new JsonArray();
    private JsonArray orders = new JsonArray();
    User(String username) {
        this.username = username;
    }

    public void addItem(String item, int quantity, String ID){
        JsonObject newItem = new JsonObject();
        newItem.addProperty("name", item);
        newItem.addProperty("quantity", quantity);
        newItem.addProperty("id", ID);
        newItem.addProperty("price", quantity * 10);
        shoppingCart.add(newItem);
    }

    public int findItem(String item){
        if(shoppingCart.size() == 0){
            return -1;
        }
        else {
            int i = 0;
            boolean found = false;
            while (i < shoppingCart.size() && !found) {
                System.out.println("COmparing newItem with: " + (shoppingCart.get(i).getAsJsonObject().get("name").getAsString()));
                if ((shoppingCart.get(i).getAsJsonObject().get("name").getAsString()).equals(item)) {
                    found = true;
                } else {
                    i++;
                }
            }
            System.out.println("item at index: " + i);
            System.out.println("cart: " + shoppingCart );
            if(found){
                return i;
            }
            else{
                return -1;
            }
        }
    }

    public void replaceItem(String item, int quantity, int position) {
        String id = shoppingCart.getAsJsonArray().get(position).getAsJsonObject().get("id").getAsString();
        deleteItem(item);
        addItem(item, quantity, id);
        System.out.println("Updatesd quantity: " + shoppingCart);
    }

    public void deleteItem(String item){
        int index = findItem(item);
        shoppingCart.remove(index);
        System.out.println("Deleted: " + shoppingCart);
    }

    public JsonArray getItems(){
        System.out.println("returning: " + shoppingCart); return shoppingCart;
    }

    public String getUsername(){
        return this.username;
    }

    public void clearCart(){
        shoppingCart = new JsonArray();
    }

    public void addOrder(String date){
        JsonObject newOrder = new JsonObject();
        newOrder.add(date, shoppingCart);
        orders.add(newOrder);
    }

    public JsonArray getOrders(){
        return orders;
    }

}