from neo4j import GraphDatabase
from statistics import mean
import os

URI = "bolt://localhost:7687"
AUTH = ("neo4j", "chesschess")

#
# PATHS
#

data_dir = "data"
opening_outfile_name = os.path.join(data_dir,"world_rapid_2024_opening_occurences.csv")
relative_branching_outfile_name = os.path.join(data_dir,"world_rapid_2024_relative_branching.csv")
absolute_branching_outfile_name = os.path.join(data_dir,"world_rapid_2024_absolute_branching.csv")
existing_games_path = os.path.join(data_dir,"existing_games_up_to_depth.csv")
opening_total_path = os.path.join(data_dir,"opening_total_positions_upto_depth.csv")
unique_openings_path = os.path.join(data_dir,"opening_unique_positions_upto_depth.csv")
most_played_opening_depth = os.path.join(data_dir,"most_played_opening_upto_depth.csv")

#
# OPENING QUERIES
#

# This method executes a query which retrieves the count of games for each opening and writes the results to a CSV file.
def get_standard_opening_count(session, csv_name=opening_outfile_name):
    out_csv_path = os.path.join(os.path.dirname(__file__), csv_name)
    # This query retrieves the count of games for each opening and orders them in descending order.
    get_opening_count_query = (
        "MATCH (opening:Opening) "
        "OPTIONAL MATCH (opening) <-- (game:Game) "
        "RETURN opening.name AS opening_name, opening.eco as eco, COALESCE(count(game), 0) AS game_count "
        "ORDER BY game_count DESC "
    )
    
    result = session.run(get_opening_count_query)
    counter = 1
    with open(out_csv_path, "w", encoding="utf-8") as file:
        file.write("opening_name\teco\tgame_count\trank\n")
        for record in result:
            file.write(f"{record['opening_name']}\t{record['eco']}\t{record['game_count']}\t{counter}\n")
            counter += 1

# This method executes a query which retrieves the longest opening (move wise) and its count of games.
def get_Length_of_longest_opening(session):
    # This query retrieves the longest opening name and its count of games.
    get_longest_opening_query = (
        "MATCH (opening:Opening) "
        "OPTIONAL MATCH (opening) <-[p:position]- () "
        "RETURN opening.name AS opening_name, COALESCE(p.move, 0) AS move, COALESCE(p.ply, 0) as ply "
        "ORDER BY move DESC, ply DESC "
        "LIMIT 10 "
    )
    
    result = session.run(get_longest_opening_query)
    for record in result:
        print(f" Opening Name: {record['opening_name']}, Move Count: {record['move']}, Ply Count: {record['ply']}")

def get_total_opening_positions_upto_depth(session, depth=None, outfile_name=opening_total_path, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    
    get_opening_positions_query = (
        f"UNWIND range(1,{int(depth)}) AS depth "
        "OPTIONAL MATCH (g:Game)-[p:position]->(pos:Opening) "
        "WHERE p.ply = depth "
        "RETURN depth, count(pos) AS opening_count "
    )
    
    result = session.run(get_opening_positions_query)
        
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("depth\topening_count\n")
            for record in result:
                print(f"Depth: {record['depth']}, Opening Count: {record['opening_count']}")
                file.write(f"{record['depth']}\t{record['opening_count']}\n")
                
def get_games_with_longest_opening(session):
    get_games_query = (
        "MATCH (g:Game)-[p:position]->(pos:Opening) "
        "MATCH (g) -[:result_of_game]->(res) "
        "WITH g, p.ply AS move_count, pos.name AS opening_name, res.fen AS result "
        "ORDER BY move_count DESC "
        "RETURN g.gameNumber as game, move_count, opening_name, result "
        "LIMIT 3"
    )
    
    result = session.run(get_games_query)
    for record in result:
        print(f"Game ID: {record['game']}, Move Count: {record['move_count']}, Opening Name: {record['opening_name']}, Result: {record['result']}")                

def get_unique_opening_positions_upto_depth(session, depth=None, outfile_name=unique_openings_path, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    
    get_opening_positions_query = (
        f"UNWIND range(1,{int(depth)}) AS depth "
        "OPTIONAL MATCH (g:Game)-[p:position]->(pos:Opening) "
        "WHERE p.ply = depth "
        "RETURN depth, count(DISTINCT pos) AS opening_count "
    )
    
    result = session.run(get_opening_positions_query)
        
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("depth\topening_count\n")
            for record in result:
                print(f"Depth: {record['depth']}, Opening Count: {record['opening_count']}")
                file.write(f"{record['depth']}\t{record['opening_count']}\n")

def get_top_openings_per_depth(session, depth=None, outfile_name=most_played_opening_depth, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    get_top_openings_query = (
        f"UNWIND range(1,{int(depth)}) AS depth "
        "MATCH (g:Game)-[p:position]->(pos:Opening) "
        "WHERE p.ply = depth "
        "WITH pos.name AS opening_name, count(DISTINCT g) AS game_count, depth "
        "RETURN opening_name, game_count, depth "
        "ORDER BY game_count DESC "
    )
    result = session.run(get_top_openings_query)
    result_copy = list(result)
    openings_depth = []
    for i in range(depth):
        cur_opening = ""
        cur_count = 0
        for record in result_copy:
            tmp_depth = int(record['depth'])
            tmp_count = int(record['game_count'])
            if tmp_depth == i + 1:
                if tmp_count > cur_count:
                    cur_opening = record['opening_name']
                    cur_count = tmp_count
        openings_depth.append((i+1, cur_opening, cur_count))
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("opening_name\tdepth\tgame_count\n")
            for (depth, opening_name, game_count) in openings_depth:
                file.write(f"{opening_name}\t{depth}\t{game_count}\n")

#
# BRANCHING FACTOR QUERIES
#

# This method executes a query which retrieves the abosolute number of positions reached after x moves.       
def get_absolute_branching_factor_upto_depth(session, depth=None, outfile_name=absolute_branching_outfile_name, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")

    get_branching_factor_query = (
        f"UNWIND range(0,{int(depth)}) AS depth "
        "MATCH (g:Game) "
        "MATCH (g)-[p_meta:position]->(pos) "
        "WHERE p_meta.ply = depth "
        "WITH depth, count(DISTINCT pos) AS count "
        "RETURN depth, count "
        "ORDER BY depth "
    )
    
    result = session.run(get_branching_factor_query)
    branching = {}
    for record in result:
        branching[record['depth']] = int(record['count'])
        print(f"Depth: {record['depth']}, Branching Factor: {record['count']}")
    
    print("LENGTH OF BRANCHING DICT:", len(branching))
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("depth\tabsolute_branching_factor\n")
            for depth, branching_factor in branching.items():
                file.write(f"{depth}\t{branching_factor}\n")
 
def get_relative_branching_factor_at_depth(session, depth=None):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    
    depth = int(depth)
    
    get_relative_branching_factor_query = (
        "MATCH (startingPosition:Position {fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -'}) "
        f"MATCH path = (startingPosition)-[:next_move*{depth}]->(midPosition)"
        "WITH midPosition "
        "OPTIONAL MATCH (midPosition)-[next:next_move]->(endPosition) "
        "WHERE NOT 'RESULT' IN labels(endPosition) "
        "RETURN toFloat(count(DISTINCT endPosition)) / toFloat(count(DISTINCT midPosition)) AS avgBranching "
    )
    
    result = session.run(get_relative_branching_factor_query)
    for record in result:
        return record['avgBranching']
        
# This method executes a query which retrieves how "much" variation there is at a given depth.
def get_relative_branching_factor_upto_depth(session, depth=None, outfile_name=relative_branching_outfile_name, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    
    max_depth = int(depth)
    min_depth = 0
    
    get_relative_branching_factor_query = (
        "MATCH (startingPosition:Position {fen: 'rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq -'}) "
        f"MATCH path = (startingPosition)-[:next_move*{min_depth}..{max_depth}]->(midPosition) "
        "WITH midPosition, length(path) AS depth " 
        "OPTIONAL MATCH (midPosition)-[next:next_move]->(endPosition) "
        "RETURN depth, toFloat(count(DISTINCT endPosition)) / toFloat(count(DISTINCT midPosition)) AS avgBranching "
        "ORDER BY depth "
    )      
    
    branching = {}
    for record in session.run(get_relative_branching_factor_query):
        branching[record['depth']] = record['avgBranching']
        print(f"Depth: {record['depth']}, Relative Branching Factor: {record['avgBranching']}")
        
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("depth\trelative_branching_factor\n")
            for depth, branching_factor in branching.items():
                file.write(f"{depth}\t{branching_factor}\n")

#
# (ACTIVE) GAME RELATED QUERIES
#
   
def get_existing_games_up_to_depth(session, depth=None, outfile_name=existing_games_path, save=False):
    if depth is None:
        raise ValueError("Depth must be specified and cannot be None.")
    
    get_existing_games_query = (
        f"UNWIND range(0,{int(depth)}) AS depth "
        "MATCH (g:Game)-[p:position]->() "
        "WITH depth, g, count(p) AS positions "
        "WHERE positions >= depth "
        "RETURN depth, count(g) AS game_count "
    )
    
    result = session.run(get_existing_games_query)
        
    if save:
        with open(outfile_name, "w", encoding="utf-8") as file:
            file.write("depth\tnumber_of_games\n")
            for record in result:
                print(f"Depth: {record['depth']}, Number of Games: {record['game_count']}")
                file.write(f"{record['depth']}\t{record['game_count']}\n")
                

# This method executes a query which retrieves the number of games in the database.
def get_number_of_games(session):
    get_number_of_games_query = (
        "MATCH (game:Game) "
        "RETURN count(game) AS game_count "
    )
    
    result = session.run(get_number_of_games_query)
    for record in result:
        print(f"Number of games: {record['game_count']}")
    return record['game_count']

#
# WRAPPER FUNCTION THAT CALLS THE QUERIES
#
    
# This method executes the method (query) provided in the argument
def execute_query(method, **kwargs):
    with GraphDatabase.driver(URI, auth=AUTH) as driver:
        with driver.session(database="neo4j") as session:
            output = method(session, **kwargs)
        session.close()
        driver.close()
    if output:
        return output


#
# MAIN FUNCTION
#

def main():
    #execute_query(get_unique_opening_positions_upto_depth, depth=55, save=True)	
    #execute_query(get_total_opening_positions_upto_depth, depth=55, save=True)
    execute_query(get_top_openings_per_depth, depth=15, save=True)
    
if __name__ == "__main__":
    main()