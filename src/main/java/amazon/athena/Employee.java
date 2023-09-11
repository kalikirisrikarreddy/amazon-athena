package amazon.athena;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "id", "firstname", "lastname", "company", "paddress", "saddress" })
public class Employee {

	private String id;
	private String firstname;
	private String lastname;
	private String company;
	private String paddress;
	private String saddress;

	public Employee() {
		super();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getPaddress() {
		return paddress;
	}

	public void setPaddress(String paddress) {
		this.paddress = paddress;
	}

	public String getSaddress() {
		return saddress;
	}

	public void setSaddress(String saddress) {
		this.saddress = saddress;
	}

	public Employee(String id, String firstname, String lastname, String company, String paddress, String saddress) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.company = company;
		this.paddress = paddress;
		this.saddress = saddress;
	}

}
