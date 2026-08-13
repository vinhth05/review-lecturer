import { useEffect, useMemo, useState, useCallback } from "react"
import { ThemeContext } from './theme-context'

export function ThemeProvider({
  children,
  defaultTheme = "system",
  storageKey = "vite-ui-theme",
  ...props
}) {
  const [theme, setTheme] = useState(
    () => localStorage.getItem(storageKey) || defaultTheme
  )

  useEffect(() => {
    const root = window.document.documentElement

    root.classList.remove("light", "dark")

    if (theme === "system") {
      const systemTheme = window.matchMedia("(prefers-color-scheme: dark)")
        .matches
        ? "dark"
        : "light"

      root.classList.add(systemTheme)
      return
    }

    root.classList.add(theme)
  }, [theme])

  const setThemeValue = useCallback((theme) => {
      localStorage.setItem(storageKey, theme)
      setTheme(theme)
    }, [storageKey])

  const value = useMemo(() => ({
    theme,
    setTheme: setThemeValue,
  }), [setThemeValue, theme])

  return (
    <ThemeContext.Provider {...props} value={value}>
      {children}
    </ThemeContext.Provider>
  )
}
