import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import powerlaw
from scipy.stats import linregress
import os

#
# LOADING DATA
#

def load_opening_data(file_path):
    """
    Load data from a file. The file should contain the name of the openeings, how often they were played and their resulting ranking, returns the top 200
    """
    cols = ["Opening", "ECO", "Played", "Ranking"]
    file_path = os.path.join("data", file_path)
    df = pd.read_csv(file_path, sep="\t", header=0, names=cols)
    df['Ranking'] = df['Ranking'].astype(int)
    df['Played'] = df['Played'].astype(int)
    df = df[df['Played'] > 0]
    print(len(df), "openings loaded from file:", file_path)
    return df

def load_opening_depth_data(unique=False, cutoff=30):
    if unique:
        cols = ["Depth", "Opening Count"]
        file_path = os.path.join("data","opening_unique_positions_upto_depth.csv")
    else:
        cols = ["Depth", "Opening Count"]
        file_path = os.path.join("data","opening_total_positions_upto_depth.csv")
    df = pd.read_csv(file_path, sep="\t", header=0, names=cols)
    df['Depth'] = df['Depth'].astype(int)
    df['Opening Count'] = df['Opening Count'].astype(int)
    return df.head(cutoff)

def load_perft(cutoff=10):
    file_path = os.path.join("data","perft.csv")
    cols = ["Depth", "Perft"]
    df = pd.read_csv(file_path, sep="\t", header=0, names=cols)
    df = df.head(cutoff)
    df['Depth'] = df['Depth'].astype(int)
    df['Perft'] = df['Perft'].astype(int)
    print(len(df), "perft values loaded from file:", file_path)
    return df

def load_branching_data(absolute=True, cutoff=60):
    if absolute:
        cols = ["Depth", "Absolute Branching Factor"]
        file_path = "world_rapid_2024_absolute_branching.csv"
    else:
        cols = ["Depth", "Relative Branching Factor"]
        file_path = "world_rapid_2024_relative_branching.csv"
    file_path = os.path.join("data", file_path)
    df = pd.read_csv(file_path, sep="\t", header=0, names=cols)
    df['Depth'] = df['Depth'].astype(int)
    if absolute:
        df['Absolute Branching Factor'] = df['Absolute Branching Factor'].astype(int)
    else:
        df['Relative Branching Factor'] = df['Relative Branching Factor'].astype(float)
    return df.head(cutoff)

#
# VISUALIZATION FUNCTIONS
#

#
# OPENINGS
#

def visualize_opening(file_path, eco=False):
    """
    Visualize the data from the file. The file should contain the name of the openeings, how often they were played and their resulting ranking
    """
    df = load_opening_data(file_path)
    ylabel = 'Frequency'

    if eco:
        eco_played  = df.groupby('ECO')['Played'].sum().sort_values(ascending=False)
        eco_df = eco_played.reset_index()
        eco_df['Ranking'] = eco_df['Played'].rank(method='dense', ascending=False).astype(int)
        print(eco_df)
        df = eco_df
    plt.figure(figsize=(10, 6))
    plt.loglog(df['Ranking'], df['Played'], color='darkblue', label='Openings played in the tournament')

    x_ref = np.linspace(df['Ranking'].min(), df['Ranking'].max(), len(df))
    y_ref = int(df.iloc[0]["Played"]) / x_ref
    plt.loglog(x_ref, y_ref, 'r--', label='Reference line')

    plt.ylabel(ylabel)
    plt.xlabel('Ranking')
    plt.title('Power Scale of Openings Played')
    plt.xticks(fontsize=6, rotation=90)
    plt.legend()
    plt.tight_layout()
    plt.show()

def visualize_opening_unique_per_depth(cutoff=30):
    """
    Visualize the number of unique openings per depth.
    """
    
    df = load_opening_depth_data(unique=True, cutoff=cutoff)

    plt.figure(figsize=(10, 6))
    plt.plot(df['Depth'], df['Opening Count'], marker='o', color='purple', label='Openings')
    plt.xlabel('Ply')
    plt.ylabel('Unique Openings')
    plt.title('Unique Openings per Depth (World Rapid 2024)')
    plt.legend()
    plt.grid()
    plt.tight_layout()
    plt.show()

def visualize_opening_total_per_depth(cutoff=30):
    """
    Visualize the total number of openings per depth.
    """
    
    df = load_opening_depth_data(unique=False, cutoff=cutoff)

    plt.figure(figsize=(10, 6))
    plt.plot(df['Depth'], df['Opening Count'], marker='o', color='orange', label='Games')
    plt.xlabel('Ply')
    plt.ylabel('Games')
    plt.title('Number of Games following Opening Theory (World Rapid 2024)')
    plt.legend()
    plt.grid()
    plt.tight_layout()
    plt.show()

#
# BRANCHING FACTOR
#

def visualize_relative_branching(cutoff=60):
    """
    Visualize the relative branching factor data.
    """
    df = load_branching_data(absolute=False, cutoff=cutoff)
    plt.figure(figsize=(10, 6))
    plt.plot(df['Depth'], df['Relative Branching Factor'], marker='o', color='green', label='Relative Branching Factor')
    plt.xlabel('Ply')
    plt.ylabel('Relative Branching Factor')
    plt.title('Relative Branching Factor (World Rapid 2024)')
    plt.legend()
    plt.grid()
    plt.tight_layout()
    plt.show()

def visualize_absolute_branching(cutoff=60, with_games=False, with_openings=False):
    """
    Visualize the absolute branching factor data. Optionally, add the amount of active games after each ply.
    """
    df = load_branching_data(absolute=True, cutoff=cutoff)
    plt.figure(figsize=(10, 6))
    plt.plot(df['Depth'], df['Absolute Branching Factor'], marker='o', color='blue', label='Absolute Branching Factor')
    if with_games:
        file_path = os.path.join("data","existing_games_up_to_depth.csv")
        games_until_depth = pd.read_csv(file_path, sep="\t", header=0, names=["Depth", "Games"])
        games_until_depth['Depth'] = games_until_depth['Depth'].astype(int)
        games_until_depth = games_until_depth.head(cutoff)
        plt.plot(games_until_depth['Depth'], games_until_depth['Games'], marker='x', color='red', label='Active Games')
    if with_openings:
        file_path = os.path.join("data","opening_total_positions_upto_depth.csv")
        openings_until_depth = pd.read_csv(file_path, sep="\t", header=0, names=["Depth", "Opening Count"])
        openings_until_depth['Depth'] = openings_until_depth['Depth'].astype(int)
        openings_until_depth = openings_until_depth.head(cutoff)
        plt.plot(openings_until_depth['Depth'], openings_until_depth['Opening Count'], marker='x', color='orange', label='Opening Positions')
    plt.xlabel('Ply')
    plt.ylabel('Total Distinct Positions')
    plt.title('Absolute Branching Factor (World Rapid 2024)')
    plt.legend()
    plt.grid()
    plt.tight_layout()
    plt.show()
    
def visualize_relative_branching_2(cutoff=60):
    """
    Visualize the relative branching factor data.
    """
    df = load_branching_data(absolute=True, cutoff=cutoff)
    plt.figure(figsize=(10, 6))
    df['Relative Branching Factor'] = df["Absolute Branching Factor"] / df["Absolute Branching Factor"].shift(1)
    plt.plot(df['Depth'], df['Relative Branching Factor'], marker='o', color='green', label='Relative Branching Factor')
    print(df.head(20))
    plt.xlabel('Ply')
    plt.ylabel('Relative Branching Factor')
    plt.title('Relative Branching Factor vs Perft Values (World Rapid 2024)')
    plt.legend()
    plt.grid()
    plt.tight_layout()
    plt.show()
    
def visualize_absolute_branching_versus_perft(cutoff=10):
    
    """
    Visualize the absolute branching factor versus perft data.
    """
    df = load_branching_data(absolute=True, cutoff=cutoff)
    perft_df = load_perft(cutoff=cutoff)

    plt.figure(figsize=(10, 6))
    perft_df = perft_df.head(cutoff)
    fraction = (df['Absolute Branching Factor'] / perft_df['Perft']) *100
    plt.bar(df['Depth'], fraction, color='green', label='Explored Positions in the Torunament')
    #plt.plot(df['Depth'], df['Absolute Branching Factor'], marker='o', color='blue', label='Absolute Branching Factor')
    #plt.plot(perft_df['Depth'], perft_df['Perft'], marker='x', color='red', label='Perft Values')
    
    plt.xlabel('Ply')
    plt.ylabel('Fraction of Absolute Branching Factor to Perft Values (%)')
    plt.title('Absolute Branching Factor vs Perft Values (World Rapid 2024)')
    plt.legend()
    plt.tight_layout()
    plt.show()
    
#    
# EXTRA   
#    
    
def visualize_and_compute_power_law_openings(opening = "world_rapid_2024_opening_occurences.csv"):
    df = load_opening_data(opening)
    print(df['Played'])
    fit = powerlaw.Fit(df['Played'], xmin=3)
    alpha = fit.power_law.alpha
    xmin = fit.power_law.xmin
    print(f"Alpha: {alpha}")
    print(f"Xmin: {xmin}")
    
    rank = df['Ranking']
    frquency = df['Played']
    log_rank = np.log10(rank)
    log_frequency = np.log(frquency)
    slope, intercept, r_value, p_value, std_err = linregress(log_rank, log_frequency)
    zipf_exponent = -slope  # because f(r) ∝ 1 / r^s
    print(f"Estimated Zipf exponent: {zipf_exponent:.3f}")
    print(f"R²: {r_value**2:.3f}")
    
    plt.figure(figsize=(10, 6))
    fig = fit.plot_pdf(color='b', linewidth=2, label='Opening Occurrences')
    fit.power_law.plot_pdf(color='r', linestyle='--', label='Power Law Fit')
    plt.xlabel('Openings')
    plt.ylabel('Popularity')
    plt.title('Power Law Distribution of Openings')
    plt.legend()
    plt.show()
    
#
# MAIN
#
    
if __name__ == "__main__":
    data_path = "data"
    opening_file = os.path.join(data_path,"world_rapid_2024_opening_occurences.csv")
    #visualize_opening_unique_per_depth()
    #visualize_absolute_branching(with_games=True, with_openings=True)
    #visualize_and_compute_power_law_openings()
    visualize_opening("world_rapid_2024_opening_occurences.csv")
    