#
#   This script is designed to fix the files containing the openings in PGN format.
#   The pgn-parser library used in the project needs the PGN file to have the fields
#   "Events", "White" and "Black" to work.
#   With CTRL+F and replaceing, I was able to convert "Site" to "Event".
#   Still, for some openings, black is missing which will be added now.
#
#   In the project, the name of the opneing will be build in the following
#   way: '"white player":"black player"'
#   where the "white player" value actually contains the main opening name and the
#   "black player" value contains the Variation. If no variation is present, the value
#   will be empty, meaning just: '"white player"'.
#
#   Additionally, add the Eco code to the opening name, which can be retrieved from the event name (in this case).
#   Library is strict: which is why the order ist the following: Event, White, Black, ECO and then PGN of opening.
#
#   Lastly, it is important for the library, that the "match"/pgn ends - meaning endings with 1-0 or 0-1 or 1/2-1/2 (which one is not important).
#

PATH_BROKEN = "openings_unfixed.pgn"
PATH_FIXED = "openings.pgn"

with open(PATH_BROKEN, "r") as broken_file:
    # Flag to check if the white player name is present in the previous line, if yes and 
    # black player name is not present, then add the black player name.
    white_value = False
    current_line_black = False
    not_meta = False
    with open(PATH_FIXED, "w") as fixed_file:
        for line in broken_file:
            if white_value:
                # Check if the line contains the "Black" field
                if not line.startswith("[Black"):
                    # Write the fixed line to the new file
                    line = '[Black ""]\n' + eco_value + line
                else:
                    current_line_black = True
                white_value = False
                
            if not_meta and line == "\n":
                fixed_file.write("1-0\n")
                not_meta = False
            fixed_file.write(line)
            
            if line.startswith("[Event"):
                # Get the event field containing the ECO code
                eco_value = line.replace("Event", "ECO")
            if line.startswith("[White"):
                white_value = True
            if current_line_black:
                fixed_file.write(eco_value)
            current_line_black = False
            
                
            if not line.startswith("[") and not len(line) == 1:
                not_meta = True
                
    with open(PATH_FIXED, "a") as fixed_file:
        fixed_file.write("1-0\n")
            