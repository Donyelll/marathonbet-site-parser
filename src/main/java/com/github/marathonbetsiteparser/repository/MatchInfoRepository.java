package com.github.marathonbetsiteparser.repository;


import com.github.marathonbetsiteparser.entities.MatchInfo;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class MatchInfoRepository {

    private List<MatchInfo> matches = new ArrayList<>();

    public void saveMatch(MatchInfo matchInfo){
        matches.add(matchInfo);
    }

    public List<MatchInfo> getMatches() {
        return matches;
    }
}
