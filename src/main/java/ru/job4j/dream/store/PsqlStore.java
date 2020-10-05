package ru.job4j.dream.store;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import ru.job4j.dream.model.User;


public class PsqlStore implements Store{

    private static final Logger LOG = LogManager.getLogger(PsqlStore.class);

    private final BasicDataSource pool = new BasicDataSource();

    private PsqlStore() {
        Properties cfg = new Properties();
        try (BufferedReader io = new BufferedReader(
            new FileReader("db.properties")
        )) {
            cfg.load(io);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        pool.setDriverClassName(cfg.getProperty("jdbc.driver"));
        pool.setUrl(cfg.getProperty("jdbc.url"));
        pool.setUsername(cfg.getProperty("jdbc.username"));
        pool.setPassword(cfg.getProperty("jdbc.password"));
        pool.setMinIdle(5);
        pool.setMaxIdle(10);
        pool.setMaxOpenPreparedStatements(100);
    }

    private static final class Lazy {
        private static final Store INST = new PsqlStore();
    }

    public static Store instOf() {
        return Lazy.INST;
    }

    @Override
    public Collection<Post> findAllPosts() {
        List<Post> posts = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("SELECT * FROM post")
        ) {
            try (ResultSet it = ps.executeQuery()) {
                while (it.next()) {
                    posts.add(new Post(it.getInt("id"), it.getString("name")));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return posts;
    }

    @Override
    public Collection<Candidate> findAllCandidates() {
        List<Candidate> candidates = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                 "SELECT * FROM candidate")) {
            try (ResultSet it = ps.executeQuery()){
                while (it.next()) {
                    Candidate candidate = new Candidate(it.getInt("id"),
                        it.getString("name"));
                    candidate.setPhotoId(it.getInt("photoid"));
                    candidates.add(candidate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return candidates;
    }

    @Override
    public void save(Post post) {
        if (post.getId() == 0) {
            create(post);
        } else {
            update(post);
        }
    }

    @Override
    public void saveCandidate(Candidate candidate) {
        if (candidate.getId() == 0) {
            createCandidate(candidate);
        } else {
            updateCandidate(candidate);
        }
    }

    private Post create(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO post(name) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    post.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return post;
    }

    private Candidate createCandidate(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("INSERT INTO candidate(name, photoid) VALUES (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    candidate.setId(id.getInt(1));
                    candidate.setPhotoId(id.getInt(2));
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return candidate;
    }

    private void update(Post post) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("UPDATE post set name=? WHERE id=?", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, post.getName());
            ps.setInt(2, post.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void updateCandidate(Candidate candidate) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps =  cn.prepareStatement("UPDATE candidate set name=?, photoid=? WHERE id=?", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, candidate.getName());
            ps.setInt(2, candidate.getPhotoId());
            ps.setInt(3, candidate.getId());
            ps.execute();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Post findPostById(int id) {
        Post result = null;
        try (Connection cn = pool.getConnection();
            PreparedStatement pr = cn.prepareStatement("SELECT * FROM post WHERE id = ?")) {
            pr.setInt(1, id);
            ResultSet rs = pr.executeQuery();
            if (rs.next()) {
                result = new Post(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Candidate findCandidateById(int id) {
        Candidate result = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement pr = cn.prepareStatement("SELECT * FROM candidate WHERE id = ?")) {
            pr.setInt(1, id);
            ResultSet rs = pr.executeQuery();
            if (rs.next()) {
                result = new Candidate(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void saveUser(User user) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("INSERT INTO users(name, email, password) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)
        ) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword());
            ps.execute();
            try (ResultSet id = ps.getGeneratedKeys()) {
                if (id.next()) {
                    user.setId(id.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findUserById(int id) {
        User result = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement pr = cn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            pr.setInt(1, id);
            ResultSet rs = pr.executeQuery();
            if (rs.next()) {
                result = new User(rs.getInt("id"), rs.getString("name"), rs.getString("email"), rs.getString("password"));
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public Collection<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement(
                 "SELECT * FROM users")) {
            try (ResultSet it = ps.executeQuery()){
                while (it.next()) {
                    User user = new User(it.getInt("id"),
                        it.getString("name"));
                    user.setEmail(it.getString("email"));
                    user.setPassword(it.getString("password"));
                    users.add(user);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public void deleteUser(User user) {
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("DElETE FROM users WHERE id=?")) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByEmail(String email) {
        User user = null;
        try (Connection cn = pool.getConnection();
             PreparedStatement ps = cn.prepareStatement("SELECT * FROM users where email = ?")) {
            ps.setString(1, email);
            ResultSet it = ps.executeQuery();
            while (it.next()) {
                user = new User(it.getInt("id"),
                    it.getString("name"),
                    it.getString("email"),
                    it.getString("password"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }
}
