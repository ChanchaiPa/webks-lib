package ii.am.ticket.entity;

import java.util.List;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;


public class ProblemDetailDao {
	private ConnectionSource connectionSource = null;

	public ProblemDetailDao(ConnectionSource connectionSource) {
		this.connectionSource = connectionSource;
	}
	
	public ProblemDetailEntity findById(int detail_id) throws Exception {
		Dao<ProblemDetailEntity, Integer> problemdetailDao = DaoManager.createDao(connectionSource, ProblemDetailEntity.class);
		return problemdetailDao.queryForId(detail_id);
	}

	public List<ProblemDetailEntity> findByTicket(int ticket_id) throws Exception {
		Dao<ProblemDetailEntity, Integer> problemdetailDao = DaoManager.createDao(connectionSource, ProblemDetailEntity.class);
		return problemdetailDao
				.queryBuilder().where().eq("ticket_id", ticket_id)
				.queryBuilder().orderBy("trans_id", false)
				.query();		
	}
	
	public int insert(ProblemDetailEntity pdEntity) throws Exception {
		Dao<ProblemDetailEntity, Integer> problemdetailDao = DaoManager.createDao(connectionSource, ProblemDetailEntity.class);
		return problemdetailDao.create(pdEntity);
	}
}
