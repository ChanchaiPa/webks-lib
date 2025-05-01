package ii.am.ticket.model;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
public class SearchModel<T> {
	@Getter @Setter
	private int totalRec;
	
	@Getter @Setter
	private List<T> list;
}
