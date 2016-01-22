package geneos_notification.objects;

public class CustomDataView {

    private String xpath;

    public CustomDataView(String path) {
        this.xpath = path;
    }

    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String path)
    {
    	xpath = path;
    }
}
