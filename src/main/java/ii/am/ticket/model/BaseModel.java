package ii.am.ticket.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class BaseModel {
	private String code;
	private String name;
	private String desc;
}
