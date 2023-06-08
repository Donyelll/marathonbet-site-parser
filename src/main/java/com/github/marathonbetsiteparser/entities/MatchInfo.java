package com.github.marathonbetsiteparser.entities;


import lombok.*;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class MatchInfo {

    private String startingTime;

    private String firstParticipant;

    private String secondParticipant;

    private String tournamentName;

    private String sportsType;

    private String matchLink;

}
