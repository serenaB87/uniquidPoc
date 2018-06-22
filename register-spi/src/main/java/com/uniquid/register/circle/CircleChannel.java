package com.uniquid.register.circle;

public class CircleChannel {
	

	private Integer id;
	private String name;
	
	public CircleChannel() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CircleChannel(Integer id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "CircleChannel [id=" + id + ", name=" + name + "]";
	}
	
}
