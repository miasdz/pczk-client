package itwatertop;

public class TestBean {
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		System.out.println("数据从"+this.name+"更新到"+name);
		this.name = name;
	}
}
