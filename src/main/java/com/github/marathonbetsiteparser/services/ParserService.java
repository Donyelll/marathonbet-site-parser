package com.github.marathonbetsiteparser.services;

import com.github.marathonbetsiteparser.entities.MatchInfo;

import com.github.marathonbetsiteparser.repository.MatchInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ParserService {

    private final MatchInfoRepository matchInfoRepository;

    // i chose only team play categories
    private static final String BASE_URL = "https://www.marathonbet.by/su/betting/22712,537,1895085,11,6,1037392,237218,4,52914,5?cpcids=all&cppcids=all&interval=ALL_TIME";

    private static Map<String,String> MONTH_MAP = new HashMap<>();

    @Autowired
    public ParserService(MatchInfoRepository matchInfoRepository) {
        this.matchInfoRepository = matchInfoRepository;
    }

    public void parseSite() {

        initializeMap();
        RestTemplate restTemplate = new RestTemplate();

        String allTournamentsRawHTML = restTemplate.getForObject(BASE_URL, String.class);

        // regex to get links to tournaments from raw html
        Pattern pattern = Pattern.compile("(?<=class=\"category-label-link\")( href=\")(.*\\d)(\")");
        Matcher matcher = pattern.matcher(allTournamentsRawHTML);
        List<String> tournaments = new ArrayList<>();

        System.out.println("Started parsing...");

        while (matcher.find()){
            tournaments.add("https://www.marathonbet.by"+matcher.group(2));
        }
        for (String tournament: tournaments) {


            //if(matchInfoRepository.getMatches().size()>50) break;

            if(!tournament.contains("Player")) {

                RestTemplate matchRestTemplate = new RestTemplate();
                String tournamentHTML = matchRestTemplate.getForObject(tournament, String.class);

                if (tournamentHTML.length()>450000){
                    tournamentHTML = tournamentHTML.substring(0,450000);
                }

                // regex to get sports type from tournament raw html
                Pattern p = Pattern.compile("(?<=\"sport-category-label\")(.*\">)(.*)(<)");
                Matcher m = p.matcher(tournamentHTML);

                m.find();
                String sportsType = m.group(2);

                // regex to get tournament name from raw html
                p = Pattern.compile("(?<=nowrap\">)(.*</span>)");
                m = p.matcher(tournamentHTML);
                m.find();
                String tournamentName = m.group(1);
                tournamentName = tournamentName.replaceAll("</span>", "");
                tournamentName = tournamentName.replaceAll("<span class=\"nowrap\">", "");

                // regex to get quantity of matches of current tournament
                p = Pattern.compile("(?<=member-area-content-table)(.*)");
                m = p.matcher(tournamentHTML);

                while (m.find()){

                    // regex to get team names
                    p = Pattern.compile("(?<=data-member-link=\"true\">)(.*)(</)");
                    m = p.matcher(tournamentHTML);
                    int teamsCount = 0;
                    while (m.find()){
                        teamsCount++;
                    }

                    // needed to count matches of current tournament because every match is played by 2 teams
                    teamsCount /= 2;

                    m.reset();

                    // regex to get match link
                    Pattern linkPattern = Pattern.compile("(?<=data-event-path=\")(.*)(\">)");
                    Matcher linkMatcher = linkPattern.matcher(tournamentHTML);

                    /*
                        regex to get date and time of match
                        there are two forms of it because
                        tournaments with only one match has different
                        attribute name in html tag
                        unlike tournaments with >1 matches in it
                     */
                    Pattern datePattern = Pattern.compile("(?<=date date-with-month\">\\n)\\s*(.*)");
                    Matcher dateMatcher = datePattern.matcher(tournamentHTML);

                    for (int i = 0; i < teamsCount; i++){

                        MatchInfo matchInfo = new MatchInfo();
                        matchInfo.setSportsType(sportsType);

                        m.find();
                        matchInfo.setFirstParticipant(m.group(1));
                        m.find();
                        matchInfo.setSecondParticipant(m.group(1));

                        linkMatcher.find();
                        matchInfo.setMatchLink("https://www.marathonbet.by/su/betting/" + linkMatcher.group(1));

                        matchInfo.setTournamentName(tournamentName);

                        if (dateMatcher.find()){
                            matchInfo.setStartingTime(parseDate(dateMatcher.group(1)));
                        }else{
                            datePattern = Pattern.compile("(?<=date date-short\">\\n)\\s*(.*)");
                            dateMatcher = datePattern.matcher(tournamentHTML);
                            dateMatcher.find();
                            matchInfo.setStartingTime(parseDate(dateMatcher.group(1)));
                        }
                        matchInfoRepository.saveMatch(matchInfo);

                    }

                }

            }

        }

    }

    private String parseDate(String dateAsString){

        /*
            7 it's just a "magic number" needed for
            proper initialization of date further
            because there is no date information (only timestamp) on site
            if match will be played today
         */
        if(dateAsString.length() > 7) {
            String month = dateAsString.replaceAll("[0-9: ]", "");
            dateAsString = dateAsString.replaceAll(month, MONTH_MAP.get(month));
            dateAsString = dateAsString.replaceFirst("\\s", "");
        }
        else {
            LocalDate currentDate = LocalDate.now();
            StringBuilder sb = new StringBuilder();
            if (currentDate.getDayOfMonth()<10){
                sb.append(0);
            }
            sb.append(currentDate.getDayOfMonth())
                    .append(".");
            if(currentDate.getMonthValue()<10) {
                sb.append(0);
            }
            sb.append(currentDate.getMonthValue())
                    .append(" ")
                    .append(dateAsString);
            dateAsString = sb.toString();
        }
        return dateAsString;
    }

    static private void initializeMap(){
        MONTH_MAP.put("янв",".01");
        MONTH_MAP.put("фев",".02");
        MONTH_MAP.put("мар",".03");
        MONTH_MAP.put("апр",".04");
        MONTH_MAP.put("май",".05");
        MONTH_MAP.put("июн",".06");
        MONTH_MAP.put("июл",".07");
        MONTH_MAP.put("авг",".08");
        MONTH_MAP.put("сен",".09");
        MONTH_MAP.put("окт",".10");
        MONTH_MAP.put("ноя",".11");
        MONTH_MAP.put("дек",".12");
    }

    public List<MatchInfo> getAllMatches(){
        if(matchInfoRepository.getMatches().isEmpty()){
            parseSite();
            return matchInfoRepository.getMatches();
        }
        return matchInfoRepository.getMatches();

    }
}
