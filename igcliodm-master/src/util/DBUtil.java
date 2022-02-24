package util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil {
	
	public static void close(ResultSet rs, PreparedStatement ps, Connection cn){
			try {
				if(null!=rs) rs.close();
				if(null!=ps) ps.close();
				if(null!=cn) cn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	}

}
