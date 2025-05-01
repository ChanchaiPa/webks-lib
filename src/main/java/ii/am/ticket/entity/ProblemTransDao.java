package ii.am.ticket.entity;

import java.util.Date;
import java.util.List;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;


public class ProblemTransDao {
	private ConnectionSource connectionSource = null;

	public ProblemTransDao(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
	}
	 
	/*public ProblemTransEntity findById(int trans_id) throws Exception {
		Dao<ProblemTransEntity, Integer> problemtransDao = DaoManager.createDao(connectionSource, ProblemTransEntity.class);
		return problemtransDao.queryForId(trans_id);
	}*/
	
	/*public List<ProblemTransEntity> findByTicket(int ticket_id) throws Exception {
		Dao<ProblemTransEntity, Integer> problemtransDao = DaoManager.createDao(connectionSource, ProblemTransEntity.class);
		return problemtransDao
				.queryBuilder().where().eq("ticket_id", ticket_id)
				.queryBuilder().orderBy("trans_id", false)
				.query();
	}*/
	
	public int create(ProblemTransEntity ptEntiy) throws Exception {
		Dao<ProblemTransEntity, Integer> problemtransDao = DaoManager.createDao(connectionSource, ProblemTransEntity.class);
		return problemtransDao.create(ptEntiy);
	}
	
	public long countInbox_TicketAgent(int ticket_id, int agent_id) throws Exception {
		Dao<ProblemTransEntity, Integer> problemtransDao = DaoManager.createDao(connectionSource, ProblemTransEntity.class);
		return problemtransDao
				.queryBuilder().where().eq("ticket_id", ticket_id)
				.and().eq("to_user_id", agent_id)
				.countOf();
	}	

	public String takeOwner(int ticket_id, int take_agent_id) throws Exception {
		Dao<ProblemTransEntity, Integer> problemtransDao = DaoManager.createDao(connectionSource, ProblemTransEntity.class);
		UpdateBuilder<ProblemTransEntity, Integer> builder1 = problemtransDao.updateBuilder();
		builder1.updateColumnValue("to_picked", 1);
		builder1.updateColumnValue("pick_date", new Date());
		builder1.updateColumnValue("pick_user_id", take_agent_id);
		builder1.where().eq("ticket_id", ticket_id).and().eq("to_picked", 0);
		int count = builder1.update();
		
		String result = "";
		if (count == 0) {
			QueryBuilder<ProblemTransEntity, Integer> builder2 = problemtransDao.queryBuilder();
			builder2.where().eq("ticket_id", ticket_id);
			builder2.orderBy("trans_id", false);
			builder2.limit(1L);
			ProblemTransEntity rs = builder2.queryForFirst();
			result = "Currenty Take Owner by=" + rs.getPick_user_id();
		}
		else 
			result = "Success";
		return result;
	}
	
}
