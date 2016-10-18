package sc.app.smartdapter.configuration;

public class RemoteBeanConfiguration {
	String id;
	String remote_url;
	String sp_url;

	public String getRemote_url() {
		return remote_url;
	}

	public void setRemote_url(String remote_url) {
		this.remote_url = remote_url;
	}

	public String getSp_url() {
		return sp_url;
	}

	public void setSp_url(String sp_url) {
		this.sp_url = sp_url;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "RemoteBeanConfiguration [id=" + id + ", remote_url=" + remote_url + ", sp_url=" + sp_url + "]";
	}

}
