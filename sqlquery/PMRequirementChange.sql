select * from (
select isnull(today.Symbol,yesterday.Symbol) as Sym, today.Requirement as tR, yesterday.Requirement as yR, (isnull(today.Requirement,0) - isnull(yesterday.Requirement,0)) as Change from
((select Symbol, Requirement from Clearing.dbo.PMRequirement
where ImportDate = CAST('11/27/2013' as DATE)) as today
full outer join
(select Symbol, Requirement from [Clearing].[dbo].[PMRequirement]
where ImportDate =(select MAX(importDate) from Clearing.dbo.PMRequirement where ImportDate < CAST('11/27/2013' as DATE))) as yesterday
on today.Symbol = yesterday.Symbol) ) as tmp
where tmp.Change <> 0
order by Change desc