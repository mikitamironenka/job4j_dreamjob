package ru.job4j.dream.store;

import ru.job4j.dream.model.Candidate;
import ru.job4j.dream.model.Post;

public class PsqlMain {

    public static void main(String[] args) {
        Store store = PsqlStore.instOf();
        store.save(new Post(0, "Junior Java Job"));
        store.save(new Post(1, "Middle Java Job"));
        store.save(new Post(2, "Senior Java Job"));
        store.saveCandidate(new Candidate(0, "Junior Java developer"));
        store.saveCandidate(new Candidate(1, "Middle Java developer"));
        store.saveCandidate(new Candidate(2, "Senior Java developer"));
        for (Post post : store.findAllPosts()) {
            System.out.println(post.getId() + " " + post.getName());
        }
        System.out.println("---------------");
        store.save(new Post(2, "Middle C++ Job"));
        for (Post post : store.findAllPosts()) {
            System.out.println(post.getId() + " " + post.getName());
        }
        System.out.println("---------------");
        System.out.println(store.findPostById(1).getName());
        System.out.println("---------------");
        for (Candidate can : store.findAllCandidates()) {
            System.out.println(can.getId() + " " + can.getName());
        }

    }
}
