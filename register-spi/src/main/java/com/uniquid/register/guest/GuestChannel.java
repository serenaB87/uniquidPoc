package com.uniquid.register.guest;

public class GuestChannel {
	
	private Integer id;
	private Integer idCircle;
	private String name;
	private String code;
	private Boolean isActive;

	public GuestChannel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public GuestChannel(Integer id, Integer idCircle, String name, String code, Boolean isActive) {
		super();
		this.id = id;
		this.idCircle = idCircle;
		this.name = name;
		this.code = code;
		this.isActive = isActive;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getIdCircle() {
		return idCircle;
	}

	public void setIdCircle(Integer idCircle) {
		this.idCircle = idCircle;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public String toString() {
		return "GuestChannel [id=" + id + ", idCircle=" + idCircle + ", name=" + name + ", code=" + code + ", isActive="
				+ isActive + "]";
	}
	
}
