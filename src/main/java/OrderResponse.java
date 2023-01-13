public class OrderResponse {
    private Order order;

    public OrderResponse(Order order) {
        this.order = order;
    }

    public OrderResponse() {
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String[] getColorFromOrder(){
        return order.getColor();
    }
}
