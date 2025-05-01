package ii.am.ticket.entity;

import java.util.Date;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@NoArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@DatabaseTable(tableName = "problem_trans")
public class ProblemTransEntity {
	
	@ToString.Include
	@DatabaseField(canBeNull = false)
	private Integer	ticket_id;
	
	@ToString.Include
	@DatabaseField(generatedIdSequence="problem_trans_trans_id_seq")
	private Integer	trans_id;
	
	@DatabaseField(canBeNull = false)
	private Date	trans_date;
    
	@DatabaseField(canBeNull = false)
	private Integer	to_user_id;
    
	@DatabaseField
	private Integer	to_group_id;
    
	@DatabaseField(canBeNull = false)
	private Integer	transfered_by;
    
	@DatabaseField(canBeNull = false)
	private Integer	trans_type;
    
	@DatabaseField
	private Integer	to_picked;
    
	@DatabaseField
    private Date	pick_date;
    
	@DatabaseField
    private Integer	pick_user_id;
}
