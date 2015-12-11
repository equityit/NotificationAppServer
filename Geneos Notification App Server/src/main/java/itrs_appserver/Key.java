package itrs_appserver;

public class Key {

	    private final String content;
	    private final long id;

	    public Key(String content, long id) {
	        this.content = content;
	        this.id = id;
	    }

	    public String getContent() {
	        return content;
	    }
	    
	    public long getID()
	    {
	    	return id;
	    }
	}

