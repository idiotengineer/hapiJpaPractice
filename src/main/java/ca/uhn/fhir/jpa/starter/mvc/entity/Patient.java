package ca.uhn.fhir.jpa.starter.mvc.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@AllArgsConstructor
@Setter
@Getter
@NoArgsConstructor
public class Patient {

	@Id
	private String id;

	@Column
	private String name;

	@Column
	private String address;

	@Column
	private String gender;
}
