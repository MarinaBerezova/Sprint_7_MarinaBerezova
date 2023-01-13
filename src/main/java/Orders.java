public class Orders {

    private Order[] orders;

    public Orders(Order[] orders) {
        this.orders = orders;
    }

    public Orders() {
    }

    public Order[] getOrders() {
        return orders;
    }

    public void setOrders(Order[] orders) {
        this.orders = orders;
    }
}
