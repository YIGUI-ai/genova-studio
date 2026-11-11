-- Accessibility controller for the Genova-only demonstration.
-- Run through run_demo.py so paths and timing remain reproducible.
on run argv
    if (count of argv) < 1 then error "Expected an Excel workbook path."
    set workbookPath to item 1 of argv
    tell application "System Events"
        tell process "GENOVA Studio"
            set frontmost to true
            delay 2
            click button "Import CSV / Excel" of window 1
            delay 2
            keystroke "g" using {command down, shift down}
            delay 1
            keystroke workbookPath
            delay 1
            key code 36
            delay 3
            click checkbox "rater" of window 1
            click checkbox "item" of window 1
            delay 1
            set nestedMenus to buttons of window 1 whose description is "Crossed / not nested"
            if (count of nestedMenus) > 0 then
                click item (count of nestedMenus) of nestedMenus
                delay 1
                click menu item "rater" of menu 1 of window 1
            end if
            delay 1
            set fields to every text field of window 1
            if (count of fields) > 1 then
                set value of item (count of fields) of fields to "1,2,3,4"
            end if
            delay 1
            click button "Run analysis" of window 1
            delay 8
            click tab "ANOVA" of tab group 1 of window 1
            delay 4
            click tab "Variance components" of tab group 1 of window 1
            delay 4
            click tab "D-study" of tab group 1 of window 1
            delay 5
            click tab "D-study heatmap" of tab group 1 of window 1
            delay 2
            click button "Generate heatmap" of window 1
            delay 8
            click tab "3D plot" of tab group 1 of window 1
            delay 2
            click button "Generate grid" of window 1
            delay 8
            click button "Export results (Excel)" of window 1
        end tell
    end tell
end run
