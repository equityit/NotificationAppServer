package geneos_notification.objects;

public class CustomDataView {

    private String entity;
    private String xpath;

    public CustomDataView(String ent, String path) {
        this.entity = ent;
        this.xpath = path;
    }

    public String getEntity() {
        return entity;
    }

    public String getXpath() {
        return xpath;
    }
    
    public void setXpath(String path)
    {
    	xpath = path;
    }
}
