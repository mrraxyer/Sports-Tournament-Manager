import jsPDF from 'jspdf'
import 'jspdf-autotable'
import type { Partido, TablaPosiciones } from '../api/matches'

export function useTournamentPdf() {

  function buildPdf(
    tournamentName: string,
    tipoFormato: string,
    partidos: Partido[],
    standings: TablaPosiciones[],
  ): jsPDF {
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' })
    const PAGE_W = doc.internal.pageSize.getWidth()
    const MARGIN = 14

    // Encabezado de sección
    function sectionTitle(text: string, y: number): number {
      doc.setFontSize(11)
      doc.setFont('helvetica', 'bold')
      doc.setTextColor(30, 30, 30)
      doc.text(text, MARGIN, y)
      doc.setDrawColor(200, 200, 200)
      doc.line(MARGIN, y + 1.5, PAGE_W - MARGIN, y + 1.5)
      return y + 8
    }

    // Portada
    doc.setFillColor(30, 41, 59)
    doc.rect(0, 0, PAGE_W, 50, 'F')

    doc.setFontSize(22)
    doc.setFont('helvetica', 'bold')
    doc.setTextColor(255, 255, 255)
    doc.text(tournamentName, PAGE_W / 2, 22, { align: 'center' })

    doc.setFontSize(10)
    doc.setFont('helvetica', 'normal')
    doc.setTextColor(203, 213, 225)
    doc.text(tipoFormato || 'Torneo', PAGE_W / 2, 31, { align: 'center' })

    const generado = new Date().toLocaleDateString('es-ES', { dateStyle: 'long' })
    doc.text(`Generado el ${generado}`, PAGE_W / 2, 39, { align: 'center' })

    // Resumen de estadísticas
    const played = partidos.filter(p => p.jugado).length
    const pending = partidos.filter(p => !p.jugado).length
    const goals = partidos.filter(p => p.jugado).reduce((s, p) => s + p.golesLocal + p.golesVisitante, 0)

    doc.setFillColor(248, 250, 252)
    doc.rect(MARGIN, 56, PAGE_W - MARGIN * 2, 22, 'F')

    const stats = [
      { label: 'Total partidos', value: String(partidos.length) },
      { label: 'Jugados', value: String(played) },
      { label: 'Pendientes', value: String(pending) },
      { label: 'Goles totales', value: String(goals) },
    ]
    const cellW = (PAGE_W - MARGIN * 2) / stats.length
    stats.forEach(({ label, value }, i) => {
      const cx = MARGIN + cellW * i + cellW / 2
      doc.setFontSize(14)
      doc.setFont('helvetica', 'bold')
      doc.setTextColor(30, 30, 30)
      doc.text(value, cx, 66, { align: 'center' })
      doc.setFontSize(7)
      doc.setFont('helvetica', 'normal')
      doc.setTextColor(100, 116, 139)
      doc.text(label, cx, 72, { align: 'center' })
    })

    let curY = 88

    // Partidos agrupados por fase
    curY = sectionTitle('Calendario de Partidos', curY)

    const rows = partidos.map(p => [
      p.fase ?? (p.grupo ? `Grupo ${p.grupo}` : '-'),
      p.equipoLocal?.nombre ?? 'TBD',
      p.jugado ? `${p.golesLocal} - ${p.golesVisitante}` : 'vs',
      p.equipoVisitante?.nombre ?? 'TBD',
      p.fechaPartido
        ? new Intl.DateTimeFormat('es-ES', { dateStyle: 'short', timeStyle: 'short' }).format(new Date(p.fechaPartido))
        : '-',
    ])

    ;(doc as any).autoTable({
      startY: curY,
      head: [['Fase', 'Local', 'Resultado', 'Visitante', 'Fecha']],
      body: rows,
      margin: { left: MARGIN, right: MARGIN },
      styles: { fontSize: 8, cellPadding: 2.5, textColor: [30, 30, 30] },
      headStyles: { fillColor: [30, 41, 59], textColor: 255, fontStyle: 'bold', fontSize: 8 },
      alternateRowStyles: { fillColor: [248, 250, 252] },
      columnStyles: {
        0: { cellWidth: 28 },
        1: { cellWidth: 42, halign: 'right' },
        2: { cellWidth: 22, halign: 'center', fontStyle: 'bold' },
        3: { cellWidth: 42 },
        4: { cellWidth: 36 },
      },
    })

    // Tabla de posiciones
    if (standings.length > 0) {
      curY = (doc as any).lastAutoTable.finalY + 10
      curY = sectionTitle('Tabla de Posiciones', curY)

      const sorted = [...standings].sort((a, b) =>
        b.puntos - a.puntos || (b.golesAFavor - b.golesEnContra) - (a.golesAFavor - a.golesEnContra)
      )

      const standingRows = sorted.map((s, i) => [
        String(i + 1),
        s.equipo.nombre,
        String(s.partidosJugados),
        String(s.victorias ?? '-'),
        String(s.empates ?? '-'),
        String(s.derrotas ?? '-'),
        String(s.golesAFavor),
        String(s.golesEnContra),
        String(s.puntos),
      ])

      ;(doc as any).autoTable({
        startY: curY,
        head: [['Pos', 'Equipo', 'PJ', 'V', 'E', 'D', 'GF', 'GC', 'Pts']],
        body: standingRows,
        margin: { left: MARGIN, right: MARGIN },
        styles: { fontSize: 8, cellPadding: 2.5, halign: 'center', textColor: [30, 30, 30] },
        headStyles: { fillColor: [30, 41, 59], textColor: 255, fontStyle: 'bold', fontSize: 8 },
        alternateRowStyles: { fillColor: [248, 250, 252] },
        columnStyles: {
          0: { cellWidth: 10 },
          1: { cellWidth: 55, halign: 'left' },
          8: { fontStyle: 'bold' },
        },
      })
    }

    // Numeración de páginas
    const pageCount = (doc.internal as any).getNumberOfPages()
    for (let i = 1; i <= pageCount; i++) {
      doc.setPage(i)
      doc.setFontSize(7)
      doc.setTextColor(160, 160, 160)
      doc.text(`${i} / ${pageCount}`, PAGE_W - MARGIN, doc.internal.pageSize.getHeight() - 6, { align: 'right' })
    }

    return doc
  }

  function downloadPdf(
    tournamentName: string,
    tipoFormato: string,
    partidos: Partido[],
    standings: TablaPosiciones[],
  ) {
    const doc = buildPdf(tournamentName, tipoFormato, partidos, standings)
    const safeName = tournamentName.replace(/[^a-z0-9]/gi, '_').toLowerCase()
    doc.save(`${safeName}_torneo.pdf`)
  }

  return { downloadPdf }
}
