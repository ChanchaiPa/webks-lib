package ii.am.ticket.model;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter @Setter
@NoArgsConstructor
public class TransferModel {
	private Integer trans_id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	private Date trans_date;
	
	private Integer transfered_by;
	
	private Integer to_user_id;
	
	private Integer to_group_id;
	
	private String transfered_by_name;
	
	private String to_user_name;
	
	private String to_group_name;
	
	private Integer ticket_id;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	private Date open_date;
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
	private Date reminder_date;
	
	private Integer need_day;
	
	private Integer need_hr;
	
	private String problem_detail;
	
	private Integer system_id;
	
	private String system_id_desc;
	
	private Long agePercent = 0L;
}
