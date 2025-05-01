package ii.am.ticket.entity;

import java.util.Date;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@DatabaseTable(tableName = "problems")
public class ProblemsEntity {
	
	@ToString.Include
	@DatabaseField(id = true)
    private Integer	ticket_id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@DatabaseField(canBeNull = false) 
    private Date	open_date;
    
	@DatabaseField(canBeNull = false)
    private Integer	add_user_id;
    
	@DatabaseField(canBeNull = false)
    private Integer	owner_id;
    
	@DatabaseField(canBeNull = false)
    private Integer	group_id;
    
	@DatabaseField(width = 20)
    private String	customer_id;
    
	@DatabaseField(width = 200)
    private String 	customer_name;
    
	@DatabaseField(width = 200)
    private String 	caller_name;
    
	@DatabaseField(width = 20)
    private String	caller_phoneno;
    
	@DatabaseField
    private Integer	system_id;
    
	@DatabaseField
    private Integer	subsystem_id;
    
	@DatabaseField
    private	Integer	item_id;
    
	@DatabaseField
    private Integer	module_id;
    
	@DatabaseField(width = 20)
    private String 	call_code;
    
	@DatabaseField
    private Integer	severity_level;
    
	@DatabaseField
    private Integer	priority_level;
    
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@DatabaseField(canBeNull = false)
    private Date	reminder_date;
    
	@DatabaseField(canBeNull = false)
    private Integer	need_day;
    
	@DatabaseField(canBeNull = false)
    private Integer	need_hr;
    
	@DatabaseField(width = 4000)
    private String 	problem_detail;
    
	@DatabaseField(width = 4000)
    private String	resolved_detail;
    
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@DatabaseField(canBeNull = false)
    private	Date	modified_date; 
    
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	@DatabaseField
    private Date	close_date;
    
	@DatabaseField(canBeNull = false)
    private Integer	problem_status_id;	
	
	//******************************
	@DatabaseField(persisted = false)
	private Long agePercent = 0L;
	
	@DatabaseField(persisted = false)
	private Integer countInbox = 0;
}
