import matplotlib.pyplot as plt
import pandas as pd
import numpy as np
import os

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

def visualize_opening(file_path):
    """
    Visualize the data from the file. The file should contain the name of the openeings, how often they were played and their resulting ranking
    """
    df = load_opening_data(file_path)
    ylabel = 'Number of Games containing Opening'

    plt.figure(figsize=(10, 6))
    plt.loglog(df['Ranking'], df['Played'], color='darkblue', label='Log-log Plot of Openings Played')

    x_ref = np.linspace(df['Ranking'].min(), df['Ranking'].max(), len(df))
    y_ref = int(df.iloc[0]["Played"]) / x_ref
    plt.loglog(x_ref, y_ref, 'r--', label='Reference line')

    plt.ylabel(ylabel)
    plt.xlabel('Openings from ECO database')
    plt.title('Power Scale of Openings Played')
    plt.xticks(fontsize=6, rotation=90)
    plt.legend()
    plt.tight_layout()
    plt.show()

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

def visualize_absolute_branching(cutoff=60, with_games=False):
    """
    Visualize the absolute branching factor data. Optionally, add the amount of active games after each ply.
    """
    df = load_branching_data(absolute=True, cutoff=cutoff)
    plt.figure(figsize=(10, 6))
    plt.plot(df['Depth'], df['Absolute Branching Factor'], marker='o', color='blue', label='Absolute Branching Factor')
    if with_games:
        file_path = "existing_games_up_to_depth.csv"
        games_until_depth = pd.read_csv(file_path, sep="\t", header=0, names=["Depth", "Games"])
        games_until_depth['Depth'] = games_until_depth['Depth'].astype(int)
        games_until_depth = games_until_depth.head(cutoff)
        plt.plot(games_until_depth['Depth'], games_until_depth['Games'], marker='x', color='red', label='Active Games')
        print("OK")
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
    
    
if __name__ == "__main__":
    data_path = "data"
    opening_file = os.join.path(data_path,"world_rapid_2024_opening_occurences.csv")
    visualize_absolute_branching_versus_perft(cutoff=6)
    #visualize_relative_branching_2(cutoff=20)