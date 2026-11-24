package fitmanage.dao;

import fitmanage.Member;
import java.sql.SQLException;
import java.util.List;

public interface MemberDAO {
    List<Member> getAll() throws SQLException;
    void insert(Member member) throws SQLException;
    void update(Member member) throws SQLException;
    void nonaktifkan(String idMember) throws SQLException;
}