import { create } from 'zustand';

interface UiState {
  darkMode: boolean;
  toggleDarkMode: () => void;
  hydrateTheme: () => void;
}

export const useUiStore = create<UiState>((set, get) => ({
  darkMode: false,
  toggleDarkMode: () => {
    const next = !get().darkMode;
    document.documentElement.dataset.theme = next ? 'dark' : 'light';
    localStorage.setItem('ctu_theme', next ? 'dark' : 'light');
    set({ darkMode: next });
  },
  hydrateTheme: () => {
    const saved = localStorage.getItem('ctu_theme');
    const darkMode = saved === 'dark';
    document.documentElement.dataset.theme = darkMode ? 'dark' : 'light';
    set({ darkMode });
  }
}));
